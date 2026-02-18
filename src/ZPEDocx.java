import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import jamiebalfour.zpe.types.ZPENumber;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import jamiebalfour.generic.JBBinarySearchTree;
import jamiebalfour.zpe.core.ZPEObject;
import jamiebalfour.zpe.core.ZPERuntimeEnvironment;
import jamiebalfour.zpe.core.ZPEStructure;
import jamiebalfour.zpe.interfaces.ZPEPropertyWrapper;
import jamiebalfour.zpe.interfaces.ZPEType;
import jamiebalfour.zpe.types.ZPEBoolean;
import jamiebalfour.zpe.types.ZPEString;

public class ZPEDocx extends ZPEStructure {

  private static final long serialVersionUID = 3384128892123301192L;

  private XWPFDocument doc = null;
  private boolean closed = false;

  public ZPEDocx(ZPERuntimeEnvironment z, ZPEPropertyWrapper parent, String name) {
    super(z, parent, name);

    addNativeMethod("open", new open_Command());
    addNativeMethod("new_file", new new_Command());
    addNativeMethod("save", new save_Command());
    addNativeMethod("close", new close_Command());

    addNativeMethod("add_paragraph", new add_paragraph_Command());
    addNativeMethod("add_heading", new add_heading_Command());
    addNativeMethod("replace_all", new replace_all_Command());
    addNativeMethod("is_open", new is_open_Command());
  }

  private void ensureOpen() {
    if (doc == null || closed) {
      // You likely have a standard runtime error mechanism; adjust to your project.
      throw new RuntimeException("DOCX document is not open.");
    }
  }

  // docx.open(path)
  public class open_Command implements jamiebalfour.zpe.interfaces.ZPEObjectNativeMethod {

    @Override
    public ZPEType MainMethod(JBBinarySearchTree<String, ZPEType> parameters, ZPEObject parent) {
      try {
        String path = ((ZPEString) parameters.get("path")).toString();
        FileInputStream fis = new FileInputStream(path);
        doc = new XWPFDocument(fis);
        fis.close();
        closed = false;
        return new ZPEBoolean(true);
      } catch (IOException ex) {
        return new ZPEBoolean(false);
      }
    }

    @Override
    public String[] getParameterNames() {
      return new String[] { "path" };
    }

    @Override
    public String[] getParameterTypes() {
      return new String[] { "text" };
    }

    @Override
    public int getRequiredPermissionLevel() {
      return 3; // same as your serial plugin; bump if you treat file access differently
    }

    @Override
    public String getName() {
      return "open";
    }
  }

  // docx.new()
  public class new_file_Command implements jamiebalfour.zpe.interfaces.ZPEObjectNativeMethod {

    @Override
    public ZPEType MainMethod(JBBinarySearchTree<String, ZPEType> parameters, ZPEObject parent) {
      doc = new XWPFDocument();
      closed = false;
      return new ZPEBoolean(true);
    }

    @Override
    public String[] getParameterNames() {
      return new String[] {};
    }

    @Override
    public String[] getParameterTypes() {
      return new String[] {};
    }

    @Override
    public int getRequiredPermissionLevel() {
      return 3;
    }

    @Override
    public String getName() {
      return "new";
    }
  }

  // docx.save(path)
  public class save_Command implements jamiebalfour.zpe.interfaces.ZPEObjectNativeMethod {

    @Override
    public ZPEType MainMethod(JBBinarySearchTree<String, ZPEType> parameters, ZPEObject parent) {
      ensureOpen();
      try {
        String path = ((ZPEString) parameters.get("path")).toString();
        FileOutputStream fos = new FileOutputStream(path);
        doc.write(fos);
        fos.close();
        return new ZPEBoolean(true);
      } catch (IOException ex) {
        return new ZPEBoolean(false);
      }
    }

    @Override
    public String[] getParameterNames() {
      return new String[] { "path" };
    }

    @Override
    public String[] getParameterTypes() {
      return new String[] { "text" };
    }

    @Override
    public int getRequiredPermissionLevel() {
      return 3;
    }

    @Override
    public String getName() {
      return "save";
    }
  }

  // docx.close()
  public class close_Command implements jamiebalfour.zpe.interfaces.ZPEObjectNativeMethod {

    @Override
    public ZPEType MainMethod(JBBinarySearchTree<String, ZPEType> parameters, ZPEObject parent) {
      if (doc == null || closed) return new ZPEBoolean(true);
      try {
        doc.close();
        closed = true;
        return new ZPEBoolean(true);
      } catch (IOException ex) {
        return new ZPEBoolean(false);
      }
    }

    @Override
    public String[] getParameterNames() {
      return new String[] {};
    }

    @Override
    public String[] getParameterTypes() {
      return new String[] {};
    }

    @Override
    public int getRequiredPermissionLevel() {
      return 3;
    }

    @Override
    public String getName() {
      return "close";
    }
  }

  // docx.is_open()
  public class is_open_Command implements jamiebalfour.zpe.interfaces.ZPEObjectNativeMethod {

    @Override
    public ZPEType MainMethod(JBBinarySearchTree<String, ZPEType> parameters, ZPEObject parent) {
      return new ZPEBoolean(doc != null && !closed);
    }

    @Override
    public String[] getParameterNames() {
      return new String[] {};
    }

    @Override
    public String[] getParameterTypes() {
      return new String[] {};
    }

    @Override
    public int getRequiredPermissionLevel() {
      return 3;
    }

    @Override
    public String getName() {
      return "is_open";
    }
  }

  // docx.add_paragraph(text) -> paragraphIndex (0-based)
  public class add_paragraph_Command implements jamiebalfour.zpe.interfaces.ZPEObjectNativeMethod {

    @Override
    public ZPEType MainMethod(JBBinarySearchTree<String, ZPEType> parameters, ZPEObject parent) {
      ensureOpen();
      String text = ((ZPEString) parameters.get("text")).toString();

      XWPFParagraph p = doc.createParagraph();
      XWPFRun r = p.createRun();
      r.setText(text);

      // Return index for later styling (if you add it)
      int idx = doc.getParagraphs().size() - 1;
      return new ZPENumber(idx);
    }

    @Override
    public String[] getParameterNames() {
      return new String[] { "text" };
    }

    @Override
    public String[] getParameterTypes() {
      return new String[] { "text" };
    }

    @Override
    public int getRequiredPermissionLevel() {
      return 3;
    }

    @Override
    public String getName() {
      return "add_paragraph";
    }
  }

  // docx.add_heading(text, level) -> paragraphIndex
  public class add_heading_Command implements jamiebalfour.zpe.interfaces.ZPEObjectNativeMethod {

    @Override
    public ZPEType MainMethod(JBBinarySearchTree<String, ZPEType> parameters, ZPEObject parent) {
      ensureOpen();
      String text = ((ZPEString) parameters.get("text")).toString();
      int level = ((ZPENumber) parameters.get("level")).intValue();

      if (level < 1) level = 1;
      if (level > 6) level = 6;

      XWPFParagraph p = doc.createParagraph();
      // Word heading styles are typically "Heading1".."Heading6"
      p.setStyle("Heading" + level);

      XWPFRun r = p.createRun();
      r.setText(text);

      int idx = doc.getParagraphs().size() - 1;
      return new ZPENumber(idx);
    }

    @Override
    public String[] getParameterNames() {
      return new String[] { "text", "level" };
    }

    @Override
    public String[] getParameterTypes() {
      return new String[] { "text", "int" };
    }

    @Override
    public int getRequiredPermissionLevel() {
      return 3;
    }

    @Override
    public String getName() {
      return "add_heading";
    }
  }

  // docx.replace_all(find, replace) -> count
  // Basic implementation: replaces in runs within paragraphs.
  // Note: Word can split text across runs, so this won’t catch every possible case.
  public class replace_all_Command implements jamiebalfour.zpe.interfaces.ZPEObjectNativeMethod {

    @Override
    public ZPEType MainMethod(JBBinarySearchTree<String, ZPEType> parameters, ZPEObject parent) {
      ensureOpen();
      String find = ((ZPEString) parameters.get("find")).toString();
      String repl = ((ZPEString) parameters.get("replace")).toString();

      int count = 0;

      for (XWPFParagraph p : doc.getParagraphs()) {
        for (XWPFRun r : p.getRuns()) {
          String t = r.getText(0);
          if (t == null) continue;
          if (!t.contains(find)) continue;

          int beforeLen = t.length();
          String newText = t.replace(find, repl);

          // crude “count occurrences”:
          count += (beforeLen - newText.length()) / Math.max(1, find.length());

          r.setText(newText, 0);
        }
      }

      return new ZPENumber(count);
    }

    @Override
    public String[] getParameterNames() {
      return new String[] { "find", "replace" };
    }

    @Override
    public String[] getParameterTypes() {
      return new String[] { "text", "text" };
    }

    @Override
    public int getRequiredPermissionLevel() {
      return 3;
    }

    @Override
    public String getName() {
      return "replace_all";
    }
  }
}