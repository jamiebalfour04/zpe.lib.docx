<h1>zpe.lib.docx</h1>

<p>
  This is the official DOCX plugin for ZPE.
</p>

<h2>Installation</h2>
<p>
  Place <code>zpe.lib.docx.jar</code> in your ZPE plugins folder and restart ZPE.
</p>

<h2>Functions</h2>
<p>
  This plugin does not expose any global functions. All functionality is provided via the <code>docx</code> object.
</p>

<table>
  <thead>
    <tr>
      <th>Function</th>
      <th>Parameters</th>
      <th>Returns</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td colspan="4"><em>No functions.</em></td>
    </tr>
  </tbody>
</table>

<h2>Objects</h2>

<table>
  <thead>
    <tr>
      <th>Method</th>
      <th>Parameters</th>
      <th>Returns</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th colspan="4"><code>docx</code></th>
    </tr>

    <tr>
      <td><code>new_file</code></td>
      <td><code>()</code></td>
      <td><code>bool</code></td>
      <td>Creates a new blank DOCX document in memory.</td>
    </tr>

    <tr>
      <td><code>open</code></td>
      <td><code>(text path)</code></td>
      <td><code>bool</code></td>
      <td>Opens a DOCX document from disk.</td>
    </tr>

    <tr>
      <td><code>save</code></td>
      <td><code>(text path)</code></td>
      <td><code>bool</code></td>
      <td>Saves the current DOCX document to disk.</td>
    </tr>

    <tr>
      <td><code>close</code></td>
      <td><code>()</code></td>
      <td><code>bool</code></td>
      <td>Closes the document and releases resources. Safe to call multiple times.</td>
    </tr>

    <tr>
      <td><code>is_open</code></td>
      <td><code>()</code></td>
      <td><code>bool</code></td>
      <td>Returns <code>true</code> if the document is currently open in memory.</td>
    </tr>

    <tr>
      <td><code>add_paragraph</code></td>
      <td><code>(text text)</code></td>
      <td><code>int</code></td>
      <td>Adds a paragraph containing the given text. Returns the paragraph index (0-based).</td>
    </tr>

    <tr>
      <td><code>add_heading</code></td>
      <td><code>(text text, int level)</code></td>
      <td><code>int</code></td>
      <td>Adds a heading paragraph (levels 1–6). Returns the paragraph index (0-based).</td>
    </tr>

    <tr>
      <td><code>replace_all</code></td>
      <td><code>(text find, text replace)</code></td>
      <td><code>int</code></td>
      <td>Replaces all occurrences of <code>find</code> with <code>replace</code> in paragraph text. Returns the number of replacements made.</td>
    </tr>
  </tbody>
</table>

<h2>Example</h2>

<pre><code>import "docx"

d = docx()
d.new()

d.add_heading("My Document", 1)
d.add_paragraph("Hello from ZPE!")

d.save("output.docx")
d.close()
</code></pre>

<h2>Notes</h2>
<ul>
  <li>This plugin currently targets <code>.docx</code> only (not legacy <code>.doc</code>).</li>
  <li><code>replace_all</code> works best on simple documents; some Word documents split text across runs, which may reduce replacement coverage in certain cases.</li>
</ul>