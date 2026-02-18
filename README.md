<h1>zpe.lib.docx</h1>

<p>
  This is the official DOCX plugin for ZPE.
</p>

<p>
  The plugin provides support for creating, reading and modifying .docx files directly from ZPE.
</p>

<h2>Installation</h2>

<p>
  Place <strong>zpe.lib.docx.jar</strong> in your ZPE native-plugins folder and restart ZPE.
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

d = new docx()
d.new_file()

d.add_heading("My Document", 1)
d.add_paragraph("Hello from ZPE!")

d.save("output.docx")
d.close()
</pre>

<h2>Notes</h2>

<ul>
  <li>This plugin supports .docx files only.</li>
  <li>Requires Apache POI (bundled within the plugin).</li>
</ul>
