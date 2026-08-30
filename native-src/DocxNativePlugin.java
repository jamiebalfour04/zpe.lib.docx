import org.apache.poi.xwpf.usermodel.*;
import org.apache.logging.log4j.simple.SimpleLoggerContextFactory;
import org.graalvm.nativeimage.*;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.*;
import org.graalvm.word.WordFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

/** GraalVM adapter exposing the Java DOCX implementation to ZPEX. */
public final class DocxNativePlugin {
  static {
    // The native plugin only needs POI's logging API. Using Log4j's built-in
    // simple context avoids log4j-core's reflection-driven plugin discovery.
    System.setProperty("log4j2.loggerContextFactory", SimpleLoggerContextFactory.class.getName());
  }
  private static final ObjectHandles HANDLES=ObjectHandles.getGlobal();
  private static final Pattern STRING_VALUE=Pattern.compile("\\\"value\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\"");
  private static final Pattern NUMBER_VALUE=Pattern.compile("\\\"value\\\"\\s*:\\s*(-?[0-9]+(?:\\.[0-9]+)?)");
  private static CCharPointer descriptor=WordFactory.nullPointer();
  private static final String DESCRIPTOR="{\"abiVersion\":1,\"name\":\"libDOCX\",\"version\":\"1.1\",\"functions\":[],\"objects\":[{\"name\":\"docx\",\"constructorParameters\":[],\"properties\":[],\"methods\":["
      +"{\"name\":\"open\",\"parameters\":[{\"name\":\"path\",\"type\":\"string\"}],\"returnType\":\"boolean\"},"
      +"{\"name\":\"new_file\",\"parameters\":[],\"returnType\":\"boolean\"},"
      +"{\"name\":\"save\",\"parameters\":[{\"name\":\"path\",\"type\":\"string\"}],\"returnType\":\"boolean\"},"
      +"{\"name\":\"close\",\"parameters\":[],\"returnType\":\"boolean\"},"
      +"{\"name\":\"add_paragraph\",\"parameters\":[{\"name\":\"text\",\"type\":\"string\"}],\"returnType\":\"mixed\"},"
      +"{\"name\":\"add_heading\",\"parameters\":[{\"name\":\"text\",\"type\":\"string\"},{\"name\":\"level\",\"type\":\"number\"}],\"returnType\":\"mixed\"},"
      +"{\"name\":\"replace_all\",\"parameters\":[{\"name\":\"find\",\"type\":\"string\"},{\"name\":\"replace\",\"type\":\"string\"}],\"returnType\":\"mixed\"},"
      +"{\"name\":\"is_open\",\"parameters\":[],\"returnType\":\"boolean\"}]}]}";
  private static final class DocumentState {XWPFDocument document;boolean closed=true;void close()throws IOException{if(document!=null&&!closed)document.close();closed=true;}void requireOpen(){if(document==null||closed)throw new IllegalStateException("DOCX document is not open.");}}
  public static void main(String[] arguments){}
  private static CCharPointer c(String value){byte[] bytes=value.getBytes(StandardCharsets.UTF_8);CCharPointer out=UnmanagedMemory.malloc(bytes.length+1);for(int i=0;i<bytes.length;i++)out.write(i,bytes[i]);out.write(bytes.length,(byte)0);return out;}
  private static String s(CCharPointer value){if(value.isNull())return "";int n=0;while(value.read(n)!=0)n++;byte[] bytes=new byte[n];for(int i=0;i<n;i++)bytes[i]=value.read(i);return new String(bytes,StandardCharsets.UTF_8);}
  private static String unescape(String value){return value.replace("\\n","\n").replace("\\r","\r").replace("\\t","\t").replace("\\\"","\"").replace("\\\\","\\");}
  private static List<String> strings(String json){ArrayList<String> out=new ArrayList<>();Matcher matcher=STRING_VALUE.matcher(json);while(matcher.find())out.add(unescape(matcher.group(1)));return out;}
  private static long number(String json,long fallback){Matcher matcher=NUMBER_VALUE.matcher(json);return matcher.find()?(long)Double.parseDouble(matcher.group(1)):fallback;}
  private static CCharPointer value(String type,String raw){return c("{\"kind\":\"value\",\"value\":{\"type\":\""+type+"\",\"value\":"+raw+"}}");}
  private static CCharPointer bool(boolean v){return value("boolean",Boolean.toString(v));}private static CCharPointer num(long v){return value("number",Long.toString(v));}
  private static long retain(Object value){return HANDLES.create(value).rawValue();}private static DocumentState state(long handle){return HANDLES.get(WordFactory.pointer(handle));}
  private static void fail(CCharPointerPointer error,Throwable throwable){if(error.isNonNull())error.write(c(throwable.getMessage()==null?throwable.getClass().getSimpleName():throwable.getMessage()));}
  @CEntryPoint(name="zpe_graal_plugin_abi_version")public static int abi(IsolateThread thread){return 1;}
  @CEntryPoint(name="zpe_graal_plugin_descriptor")public static CCharPointer descriptor(IsolateThread thread){if(descriptor.isNull())descriptor=c(DESCRIPTOR);return descriptor;}
  @CEntryPoint(name="zpe_graal_plugin_create")public static long create(IsolateThread thread,CCharPointer type,CCharPointer arguments,CCharPointerPointer error){try{if(!"docx".equals(s(type)))throw new IllegalArgumentException("Unknown DOCX object.");return retain(new DocumentState());}catch(Throwable t){fail(error,t);return 0;}}
  @CEntryPoint(name="zpe_graal_plugin_invoke")public static CCharPointer invoke(IsolateThread thread,long handle,CCharPointer type,CCharPointer method,CCharPointer arguments,CCharPointerPointer error){
    DocumentState state;String name=s(method);try{state=state(handle);}catch(Throwable t){fail(error,t);return WordFactory.nullPointer();}
    try{String json=s(arguments);List<String> text=strings(json);switch(name){
      case "new_file":state.close();state.document=new XWPFDocument();state.closed=false;return bool(true);
      case "open":state.close();try(InputStream input=new FileInputStream(text.get(0))){state.document=new XWPFDocument(input);}state.closed=false;return bool(true);
      case "save":state.requireOpen();try(OutputStream output=new FileOutputStream(text.get(0))){state.document.write(output);}return bool(true);
      case "close":state.close();return bool(true);
      case "is_open":return bool(state.document!=null&&!state.closed);
      case "add_paragraph":state.requireOpen();XWPFParagraph paragraph=state.document.createParagraph();paragraph.createRun().setText(text.get(0));return num(state.document.getParagraphs().size()-1);
      case "add_heading":state.requireOpen();int level=(int)Math.max(1,Math.min(6,number(json,1)));XWPFParagraph heading=state.document.createParagraph();heading.setStyle("Heading"+level);heading.createRun().setText(text.get(0));return num(state.document.getParagraphs().size()-1);
      case "replace_all":state.requireOpen();String find=text.get(0),replacement=text.get(1);int count=0;if(!find.isEmpty())for(XWPFParagraph p:state.document.getParagraphs())for(XWPFRun run:p.getRuns()){String current=run.getText(0);if(current==null)continue;int from=0,at;while((at=current.indexOf(find,from))>=0){count++;from=at+find.length();}if(from>0)run.setText(current.replace(find,replacement),0);}return num(count);
      default:throw new IllegalArgumentException("Unknown DOCX method '"+name+"'.");}}
    catch(Throwable t){
      if("open".equals(name)){state.document=null;state.closed=true;}
      if("open".equals(name)||"new_file".equals(name)||"save".equals(name)||"close".equals(name)||"add_paragraph".equals(name)||"add_heading".equals(name)||"replace_all".equals(name))return bool(false);
      fail(error,t);return WordFactory.nullPointer();}}
  @CEntryPoint(name="zpe_graal_plugin_get_property")public static CCharPointer get(IsolateThread thread,long handle,CCharPointer type,CCharPointer property,CCharPointerPointer error){fail(error,new IllegalArgumentException("docx has no properties."));return WordFactory.nullPointer();}
  @CEntryPoint(name="zpe_graal_plugin_set_property")public static int set(IsolateThread thread,long handle,CCharPointer type,CCharPointer property,CCharPointer value,CCharPointerPointer error){fail(error,new IllegalArgumentException("docx has no writable properties."));return 1;}
  @CEntryPoint(name="zpe_graal_plugin_destroy")public static void destroy(IsolateThread thread,long handle,CCharPointer type){ObjectHandle h=WordFactory.pointer(handle);DocumentState state=HANDLES.get(h);try{state.close();}catch(IOException ignored){}HANDLES.destroy(h);}
  @CEntryPoint(name="zpe_graal_plugin_free_string")public static void free(IsolateThread thread,CCharPointer value){if(value.isNonNull())UnmanagedMemory.free(value);}
}
