import jamiebalfour.zpe.core.YASSByteCodes;
import jamiebalfour.zpe.core.ZPEFunction;
import jamiebalfour.zpe.core.ZPERuntimeEnvironment;
import jamiebalfour.zpe.core.ZPEStructure;
import jamiebalfour.zpe.interfaces.ZPECustomFunction;
import jamiebalfour.zpe.interfaces.ZPELibrary;
import jamiebalfour.zpe.interfaces.ZPEType;
import jamiebalfour.zpe.types.ZPEList;

import java.util.HashMap;
import java.util.Map;

public class Plugin implements ZPELibrary {

  @Override
  public Map<String, ZPECustomFunction> getFunctions() {
    return new HashMap<>();
  }

  @Override
  public Map<String, Class<? extends ZPEStructure>> getObjects() {
    Map m = new HashMap<>();
    m.put("docx", ZPEDocx.class);
    return m;
  }

  @Override
  public boolean supportsWindows() {
    return true;
  }

  @Override
  public boolean supportsMacOs() {
    return true;
  }

  @Override
  public boolean supportsLinux() {
    return true;
  }

  @Override
  public String getName() {
    return "libDocx";
  }

  @Override
  public String getVersionInfo() {
    return "1.0";
  }

}