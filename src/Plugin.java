import jamiebalfour.zpe.core.*;
import jamiebalfour.zpe.core.interfaces.ZPECustomFunction;
import jamiebalfour.zpe.core.interfaces.ZPELibrary;
import jamiebalfour.zpe.core.interfaces.ZPEPropertyWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class Plugin implements ZPELibrary {

  @Override
  public Map<String, ZPECustomFunction> getFunctions() {
    return new HashMap<>();
  }

  @Override
  public Map<String, BiFunction<ZPERuntimeEnvironment, ZPEPropertyWrapper, ZPEObject>> getObjects() {
    Map<String, BiFunction<ZPERuntimeEnvironment, ZPEPropertyWrapper, ZPEObject>> m = new HashMap<>();
    m.put("docx", (runtime, parent) -> new ZPEDocx(runtime, parent, "docx"));
    return m;
  }

  @Override
  public Map<String, ZPEModule> getModules() {
    return new HashMap<>();
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
    return "libDOCX";
  }

  @Override
  public String getVersionInfo() {
    return "1.1";
  }

}
