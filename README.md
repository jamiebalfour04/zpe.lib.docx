<h1>zpe.lib.docx</h1>

<p>
  This is the official DOCX plugin for ZPE.
</p>

<p>
  The plugin provides support for creating, reading and modifying .docx files directly from ZPE.
</p>

<h2>Installation</h2>

<p>
  Place <strong>zpe.lib.docx.jar</strong> in your ZPE plugins folder and restart ZPE.
</p>

<p>
  For ZPEX, build the native plugin with <code>./build-native.sh</code>, then place
  <strong>zpe.lib.docx.dylib</strong> (macOS), <strong>zpe.lib.docx.so</strong> (Linux),
  or <strong>zpe.lib.docx.dll</strong> (Windows) in ZPE's <strong>native-plugins</strong> folder.
  ZPE and ZPEX then use the same import statement shown below.
</p>

<p>
  Every push builds the native ZPEX plugin for macOS ARM64, Windows x64 and
  Linux x64 with GitHub Actions. The resulting platform libraries are available
  as workflow artifacts.
</p>

<p>
  For a self-contained ZenC executable, <code>./build-native.sh</code> also
  builds <strong>libzpe.lib.docx.a</strong> and copies it into the example's
  <strong>binary-plugins</strong> directory. You can then run
  <code>zpe -c examples/native_docx.yas -o doco --binary</code>; the resulting
  executable contains the DOCX implementation and does not require the plugin
  dylib or an installed ZPE runtime.
</p>

<p>
  You can also download with the ZULE Package Manager by using:
</p>
<p>
  <code>zpe --zule install zpe.lib.docx.jar</code>
</p>

<h2>Documentation</h2>

<p>
  Full documentation, examples and API reference are available here:
</p>

<p>
  <a href="https://www.jamiebalfour.scot/projects/zpe/documentation/plugins/zpe.lib.docx/" target="_blank">
    View the complete documentation
  </a>
</p>

<h2>Example</h2>

<pre>

import "zpe.lib.docx"

$document = new docx()
$document->new_file()

$document->add_heading("My Document", 1)
$document->add_paragraph("Hello from ZPE!")

$document->save("output.docx")
$document->close()
</pre>

<h2>Notes</h2>

<ul>
  <li>This plugin supports .docx files only.</li>
  <li>Requires Apache POI (bundled within the plugin).</li>
  <li>The native ZPEX build requires GraalVM and the dependency versions listed in <code>build-native.sh</code>.</li>
</ul>
