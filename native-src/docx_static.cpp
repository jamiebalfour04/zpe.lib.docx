#include <algorithm>
#include <cstdint>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>

namespace {
struct Paragraph { std::string text; int heading = 0; };
struct Document { std::vector<Paragraph> paragraphs; bool open = false; };

char *copy(const std::string &value) {
  char *out = static_cast<char *>(std::malloc(value.size() + 1));
  if (out) std::memcpy(out, value.c_str(), value.size() + 1);
  return out;
}
char *result(const char *type, const std::string &value) {
  return copy(std::string("{\"kind\":\"value\",\"value\":{\"type\":\"") + type + "\",\"value\":" + value + "}}");
}
char *boolean(bool value) { return result("boolean", value ? "true" : "false"); }
char *number(std::size_t value) { return result("number", std::to_string(value)); }
void fail(char **error, const std::string &message) { if (error) *error = copy(message); }

std::vector<std::string> strings(const char *json) {
  std::vector<std::string> out;
  if (!json) return out;
  const std::string source(json), marker = "\"type\":\"string\",\"value\":\"";
  std::size_t at = 0;
  while ((at = source.find(marker, at)) != std::string::npos) {
    at += marker.size(); std::string value; bool escaped = false;
    while (at < source.size()) {
      char c = source[at++];
      if (escaped) { value += c == 'n' ? '\n' : c == 'r' ? '\r' : c == 't' ? '\t' : c; escaped = false; }
      else if (c == '\\') escaped = true;
      else if (c == '"') break;
      else value += c;
    }
    out.push_back(value);
  }
  return out;
}
long first_number(const char *json, long fallback) {
  if (!json) return fallback;
  const std::string source(json), marker = "\"type\":\"number\",\"value\":";
  std::size_t at = source.find(marker); if (at == std::string::npos) return fallback;
  return std::strtol(source.c_str() + at + marker.size(), nullptr, 10);
}
std::string xml(const std::string &text) {
  std::string out;
  for (char c : text) {
    if (c == '&') out += "&amp;"; else if (c == '<') out += "&lt;";
    else if (c == '>') out += "&gt;"; else if (c == '"') out += "&quot;";
    else if (c == '\'') out += "&apos;"; else out += c;
  }
  return out;
}
void u16(std::ostream &out, std::uint16_t v) { out.put(v & 255); out.put((v >> 8) & 255); }
void u32(std::ostream &out, std::uint32_t v) { u16(out, v & 65535); u16(out, v >> 16); }
std::uint32_t crc32(const std::string &data) {
  std::uint32_t crc = 0xffffffffU;
  for (unsigned char c : data) { crc ^= c; for (int i = 0; i < 8; ++i) crc = (crc >> 1) ^ (0xedb88320U & (0U - (crc & 1U))); }
  return ~crc;
}
struct ZipEntry { std::string name, data; std::uint32_t crc = 0, offset = 0; };
bool write_docx(const Document &document, const std::string &path) {
  std::ostringstream body;
  body << "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
       << "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body>";
  for (const Paragraph &paragraph : document.paragraphs) {
    body << "<w:p>";
    if (paragraph.heading) body << "<w:pPr><w:pStyle w:val=\"Heading" << paragraph.heading << "\"/></w:pPr>";
    body << "<w:r><w:t xml:space=\"preserve\">" << xml(paragraph.text) << "</w:t></w:r></w:p>";
  }
  body << "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/></w:sectPr></w:body></w:document>";
  std::vector<ZipEntry> entries = {
    {"[Content_Types].xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/><Override PartName=\"/word/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml\"/></Types>"},
    {"_rels/.rels", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/></Relationships>"},
    {"word/document.xml", body.str()},
    {"word/_rels/document.xml.rels", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/></Relationships>"},
    {"word/styles.xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><w:styles xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\"><w:name w:val=\"Normal\"/></w:style><w:style w:type=\"paragraph\" w:styleId=\"Heading1\"><w:name w:val=\"heading 1\"/><w:basedOn w:val=\"Normal\"/><w:rPr><w:b/><w:sz w:val=\"32\"/></w:rPr></w:style></w:styles>"}
  };
  std::ofstream out(path, std::ios::binary); if (!out) return false;
  for (ZipEntry &entry : entries) {
    entry.offset = static_cast<std::uint32_t>(out.tellp()); entry.crc = crc32(entry.data);
    u32(out, 0x04034b50); u16(out, 20); u16(out, 0); u16(out, 0); u16(out, 0); u16(out, 0);
    u32(out, entry.crc); u32(out, entry.data.size()); u32(out, entry.data.size()); u16(out, entry.name.size()); u16(out, 0);
    out.write(entry.name.data(), entry.name.size()); out.write(entry.data.data(), entry.data.size());
  }
  std::uint32_t central = static_cast<std::uint32_t>(out.tellp());
  for (const ZipEntry &entry : entries) {
    u32(out, 0x02014b50); u16(out, 20); u16(out, 20); u16(out, 0); u16(out, 0); u16(out, 0); u16(out, 0);
    u32(out, entry.crc); u32(out, entry.data.size()); u32(out, entry.data.size()); u16(out, entry.name.size()); u16(out, 0); u16(out, 0); u16(out, 0); u16(out, 0); u32(out, 0); u32(out, entry.offset);
    out.write(entry.name.data(), entry.name.size());
  }
  std::uint32_t end = static_cast<std::uint32_t>(out.tellp());
  u32(out, 0x06054b50); u16(out, 0); u16(out, 0); u16(out, entries.size()); u16(out, entries.size()); u32(out, end - central); u32(out, central); u16(out, 0);
  return out.good();
}

const char *descriptor = R"({"abiVersion":1,"name":"libDOCX","version":"1.1","functions":[],"objects":[{"name":"docx","constructorParameters":[],"properties":[],"methods":[]}]})";
}

extern "C" std::uint32_t zpe_zpe_lib_docx_abi_version() { return 1; }
extern "C" const char *zpe_zpe_lib_docx_descriptor() { return descriptor; }
extern "C" std::uint64_t zpe_zpe_lib_docx_create(const char *type, const char *, char **error) {
  if (error) *error = nullptr; if (!type || std::strcmp(type, "docx")) { fail(error, "Unknown DOCX object."); return 0; }
  return reinterpret_cast<std::uint64_t>(new Document());
}
extern "C" char *zpe_zpe_lib_docx_invoke(std::uint64_t handle, const char *type, const char *method, const char *arguments, char **error) {
  if (error) *error = nullptr; if (!handle || !type || std::strcmp(type, "docx")) { fail(error, "Invalid DOCX object."); return nullptr; }
  Document &doc = *reinterpret_cast<Document *>(handle); std::string name = method ? method : ""; auto text = strings(arguments);
  if (name == "new_file") { doc.paragraphs.clear(); doc.open = true; return boolean(true); }
  if (name == "is_open") return boolean(doc.open);
  if (name == "close") { doc.open = false; return boolean(true); }
  if (!doc.open) return boolean(false);
  if (name == "add_paragraph" && !text.empty()) { doc.paragraphs.push_back({text[0], 0}); return number(doc.paragraphs.size() - 1); }
  if (name == "add_heading" && !text.empty()) { int level = static_cast<int>(std::max(1L, std::min(6L, first_number(arguments, 1)))); doc.paragraphs.push_back({text[0], level}); return number(doc.paragraphs.size() - 1); }
  if (name == "save" && !text.empty()) return boolean(write_docx(doc, text[0]));
  if (name == "replace_all" && text.size() > 1) { std::size_t count = 0; for (auto &p : doc.paragraphs) { std::size_t at = 0; while ((at = p.text.find(text[0], at)) != std::string::npos) { p.text.replace(at, text[0].size(), text[1]); at += text[1].size(); ++count; } } return number(count); }
  fail(error, "Unsupported DOCX method '" + name + "'."); return nullptr;
}
extern "C" char *zpe_zpe_lib_docx_get_property(std::uint64_t, const char *, const char *, char **error) { fail(error, "docx has no properties."); return nullptr; }
extern "C" int zpe_zpe_lib_docx_set_property(std::uint64_t, const char *, const char *, const char *, char **error) { fail(error, "docx has no writable properties."); return 1; }
extern "C" void zpe_zpe_lib_docx_destroy(std::uint64_t handle, const char *) { delete reinterpret_cast<Document *>(handle); }
extern "C" void zpe_zpe_lib_docx_free_string(char *value) { std::free(value); }
