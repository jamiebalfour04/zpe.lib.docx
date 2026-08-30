import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import jamiebalfour.zpe.core.YASSByteCodes;
import jamiebalfour.zpe.core.types.ZPENumber;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import jamiebalfour.zpe.core.ZPEObject;
import jamiebalfour.zpe.core.ZPERuntimeEnvironment;
import jamiebalfour.zpe.core.ZPEStructure;
import jamiebalfour.zpe.core.exceptions.ZPERuntimeException;
import jamiebalfour.zpe.core.interfaces.ZPEObjectNativeMethod;
import jamiebalfour.zpe.core.interfaces.ZPEPropertyWrapper;
import jamiebalfour.zpe.core.interfaces.ZPEType;
import jamiebalfour.zpe.core.types.ZPEBoolean;
import jamiebalfour.zpe.core.types.ZPEString;

/**
 * ZPEDocx
 *
 * Notes:
 * - Parameter types use ZPE's canonical names: string/number/mixed/map/list/boolean.
 * - Where methods can fail, they return boolean false (i.e. mixed return at runtime: number|boolean, etc.).
 * - Fixed: new_file native method name mismatch (was returning "new").
 * - Permissions: file I/O is 3, in-memory operations are 0.
 */
public class ZPEDocx extends ZPEStructure {

  private static final long serialVersionUID = 3384128892123301192L;

  private XWPFDocument doc = null;
  private boolean closed = false;

  public ZPEDocx(ZPERuntimeEnvironment z, ZPEPropertyWrapper parent, String name) {
    super(z, parent, name);

    addNativeMethod("open", new open_Command());
    addNativeMethod("new_file", new new_file_Command());
    addNativeMethod("save", new save_Command());
    addNativeMethod("close", new close_Command());

    addNativeMethod("add_paragraph", new add_paragraph_Command());
    addNativeMethod("add_heading", new add_heading_Command());
    addNativeMethod("replace_all", new replace_all_Command());
    addNativeMethod("is_open", new is_open_Command());
  }

  private void ensureOpen() {
    if (doc == null || closed) {
      throw new ZPERuntimeException("DOCX document is not open.");
    }
  }

  // docx.open(path) => boolean
  public class open_Command implements ZPEObjectNativeMethod {

    @Override
    public ZPEType run(HashMap<String, ZPEType> parameters, ZPEObject parent) {
      try {
        String path = parameters.get("path").toString();

        try (FileInputStream fis = new FileInputStream(path)) {
          doc = new XWPFDocument(fis);
        }

        closed = false;
        return new ZPEBoolean(true);
      } catch (Exception ex) {
        doc = null;
        closed = true;
        return new ZPEBoolean(false);
      }
    }

    @Override
    public String[] getParameterNames() {
      return new String[]{"path"};
    }

    @Override
    public String[] getParameterTypes() {
      return new String[]{"string"};
    }

    @Override
    public int getRequiredPermissionLevel() {
      return 3;
    }

    @Override
    public String getName() {
      return "open";
    }

    @Override
    public byte[] returnTypes() {
      return new byte[]{YASSByteCodes.BOOLEAN_TYPE};
    }
  }

  // docx.new_file() => boolean
  public class new_file_Command implements ZPEObjectNativeMethod {

    @Override
    public ZPEType run(HashMap<String, ZPEType> parameters, ZPEObject parent) {
      try {
        doc = new XWPFDocument();
        closed = false;
        return new ZPEBoolean(true);
      } catch (Exception e) {
        doc = null;
        closed = true;
        return new ZPEBoolean(false);
      }
    }

    @Override
    public String[] getParameterNames() {
      return new String[]{};
    }

    @Override
    public String[] getParameterTypes() {
      return new String[]{};
    }

    @Override
    public int getRequiredPermissionLevel() {
      return 0;
    }

    @Override
    public String getName() {
      // FIX: must match the name registered via addNativeMethod("new_file", ...)
      return "new_file";
    }

    @Override
    public byte[] returnTypes() {
      return new byte[]{YASSByteCodes.BOOLEAN_TYPE};
    }
  }

  // docx.save(path) => boolean
  public class save_Command implements ZPEObjectNativeMethod {

    @Override
    public ZPEType run(HashMap<String, ZPEType> parameters, ZPEObject parent) {
      try {
        ensureOpen();

        String path = parameters.get("path").toString();
        try (FileOutputStream fos = new FileOutputStream(path)) {
          doc.write(fos);
          fos.flush();
        }
        return new ZPEBoolean(true);
      } catch (Exception ex) {
        return new ZPEBoolean(false);
      }
    }

    @Override
    public String[] getParameterNames() {
      return new String[]{"path"};
    }

    @Override
    public String[] getParameterTypes() {
      return new String[]{"string"};
    }

    @Override
    public int getRequiredPermissionLevel() {
      return 3;
    }

    @Override
    public String getName() {
      return "save";
    }

    @Override
    public byte[] returnTypes() {
      return new byte[]{YASSByteCodes.BOOLEAN_TYPE};
    }
  }

  // docx.close() => boolean
  public class close_Command implements ZPEObjectNativeMethod {

    @Override
    public ZPEType run(HashMap<String, ZPEType> parameters, ZPEObject parent) {
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
      return new String[]{};
    }

    @Override
    public String[] getParameterTypes() {
      return new String[]{};
    }

    @Override
    public int getRequiredPermissionLevel() {
      return 3;
    }

    @Override
    public String getName() {
      return "close";
    }

    @Override
    public byte[] returnTypes() {
      return new byte[]{YASSByteCodes.BOOLEAN_TYPE};
    }
  }

  // docx.is_open() => boolean
  public class is_open_Command implements ZPEObjectNativeMethod {

    @Override
    public ZPEType run(HashMap<String, ZPEType> parameters, ZPEObject parent) {
      return new ZPEBoolean(doc != null && !closed);
    }

    @Override
    public String[] getParameterNames() {
      return new String[]{};
    }

    @Override
    public String[] getParameterTypes() {
      return new String[]{};
    }

    @Override
    public int getRequiredPermissionLevel() {
      return 0;
    }

    @Override
    public String getName() {
      return "is_open";
    }

    @Override
    public byte[] returnTypes() {
      return new byte[]{YASSByteCodes.BOOLEAN_TYPE};
    }
  }

  // docx.add_paragraph(text) => number | boolean(false)
  public class add_paragraph_Command implements ZPEObjectNativeMethod {

    @Override
    public ZPEType run(HashMap<String, ZPEType> parameters, ZPEObject parent) {
      try {
        ensureOpen();

        String text = parameters.get("text").toString();
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setText(text);

        int idx = doc.getParagraphs().size() - 1;
        return new ZPENumber(idx);
      } catch (Exception e) {
        return new ZPEBoolean(false);
      }
    }

    @Override
    public String[] getParameterNames() {
      return new String[]{"text"};
    }

    @Override
    public String[] getParameterTypes() {
      return new String[]{"string"};
    }

    @Override
    public int getRequiredPermissionLevel() {
      return 0;
    }

    @Override
    public String getName() {
      return "add_paragraph";
    }

    @Override
    public byte[] returnTypes() {
      return new byte[]{YASSByteCodes.BOOLEAN_TYPE, YASSByteCodes.NUMBER_TYPE};
    }
  }

  // docx.add_heading(text, level) => number | boolean(false)
  public class add_heading_Command implements ZPEObjectNativeMethod {

    @Override
    public ZPEType run(HashMap<String, ZPEType> parameters, ZPEObject parent) {
      try {
        ensureOpen();

        String text = parameters.get("text").toString();
        int level = (int) Double.parseDouble(parameters.get("level").toString());

        if (level < 1) level = 1;
        if (level > 6) level = 6;

        XWPFParagraph p = doc.createParagraph();
        p.setStyle("Heading" + level);

        XWPFRun r = p.createRun();
        r.setText(text);

        int idx = doc.getParagraphs().size() - 1;
        return new ZPENumber(idx);
      } catch (Exception e) {
        return new ZPEBoolean(false);
      }
    }

    @Override
    public String[] getParameterNames() {
      return new String[]{"text", "level"};
    }

    @Override
    public String[] getParameterTypes() {
      return new String[]{"string", "number"};
    }

    @Override
    public int getRequiredPermissionLevel() {
      return 0;
    }

    @Override
    public String getName() {
      return "add_heading";
    }

    @Override
    public byte[] returnTypes() {
      return new byte[]{YASSByteCodes.BOOLEAN_TYPE, YASSByteCodes.NUMBER_TYPE};
    }
  }

  // docx.replace_all(find, replace) => number | boolean(false)
  // Note: Word can split text across runs; this is "best effort" replacement within runs.
  public class replace_all_Command implements ZPEObjectNativeMethod {

    @Override
    public ZPEType run(HashMap<String, ZPEType> parameters, ZPEObject parent) {
      try {
        ensureOpen();

        String find = parameters.get("find").toString();
        String repl = parameters.get("replace").toString();

        if (find == null || find.isEmpty()) return new ZPENumber(0);

        int count = 0;

        for (XWPFParagraph p : doc.getParagraphs()) {
          for (XWPFRun r : p.getRuns()) {
            String t = r.getText(0);
            if (t == null) continue;
            if (!t.contains(find)) continue;

            // count occurrences in this run
            int from = 0;
            while (true) {
              int at = t.indexOf(find, from);
              if (at < 0) break;
              count++;
              from = at + find.length();
            }

            r.setText(t.replace(find, repl), 0);
          }
        }

        return new ZPENumber(count);
      } catch (Exception e) {
        return new ZPEBoolean(false);
      }
    }

    @Override
    public String[] getParameterNames() {
      return new String[]{"find", "replace"};
    }

    @Override
    public String[] getParameterTypes() {
      return new String[]{"string", "string"};
    }

    @Override
    public int getRequiredPermissionLevel() {
      return 0;
    }

    @Override
    public String getName() {
      return "replace_all";
    }

    @Override
    public byte[] returnTypes() {
      return new byte[]{YASSByteCodes.NUMBER_TYPE};
    }

  }
}
