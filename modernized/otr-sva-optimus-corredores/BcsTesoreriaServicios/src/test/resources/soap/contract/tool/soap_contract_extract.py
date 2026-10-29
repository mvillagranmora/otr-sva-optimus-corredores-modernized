#!/usr/bin/env python3
"""Static SOAP/JAXB contract extractor for the legacy BcsTesoreriaWebServices (BCP services).

Parses the legacy *.java sources as TEXT (no compilation, no javax.* jars, no network) and emits one
deterministic JSON contract per service plus CONTRACT-SUMMARY.md.

Usage:
  py -3 -I soap_contract_extract.py --legacy-dir <.../webservices/recaudacion> --out-dir <dir> [--legacy-root <repo root>]

What it is: a syntactic read of the annotations that JAX-WS/JAXB would read at runtime.
What it is NOT: a WSDL generator, a schema validator or a wire-level capture (see README.md).
Defaults of the annotations (jakarta.* 4.x / jakarta.jws 3.0) are filled in so that the JSON can be compared
with what reflection returns on the modern classes (SoapContratoEstaticoTest).
Unknown annotations make the tool fail on purpose (nothing is silently dropped).
"""
import argparse
import json
import os
import re
import sys

# ----------------------------------------------------------------------------------------------
# annotation defaults (simple name -> {attr: default}); values use the same convention as the Java
# reflective dump: String, boolean, enum constant name, class simple name, list for arrays.
# ----------------------------------------------------------------------------------------------
ANN_DEFAULTS = {
    "XmlType": {"name": "##default", "namespace": "##default", "propOrder": [""],
                "factoryClass": "DEFAULT", "factoryMethod": ""},
    "XmlRootElement": {"name": "##default", "namespace": "##default"},
    "XmlAccessorType": {"value": "PUBLIC_MEMBER"},
    "XmlElement": {"name": "##default", "namespace": "##default", "required": False, "nillable": False,
                   "defaultValue": "\u0000", "type": "DEFAULT"},
    "XmlAttribute": {"name": "##default", "namespace": "##default", "required": False},
    "XmlSchemaType": {"name": None, "namespace": "http://www.w3.org/2001/XMLSchema", "type": "DEFAULT"},
    "XmlEnum": {"value": "String"},
    "XmlEnumValue": {"value": None},
    "XmlSeeAlso": {"value": None},
    "XmlRegistry": {},
    "XmlElementDecl": {"namespace": "##default", "name": None, "scope": "GLOBAL",
                       "substitutionHeadNamespace": "##default", "substitutionHeadName": "",
                       "defaultValue": "\u0000"},
    "XmlSchema": {"xmlns": [], "namespace": "", "elementFormDefault": "UNSET",
                  "attributeFormDefault": "UNSET", "location": "##generate"},
    "WebService": {"name": "", "targetNamespace": "", "serviceName": "", "portName": "",
                   "wsdlLocation": "", "endpointInterface": ""},
    "WebServiceClient": {"name": "", "targetNamespace": "", "wsdlLocation": ""},
    "WebEndpoint": {"name": ""},
    "WebMethod": {"operationName": "", "action": "", "exclude": False},
    "WebParam": {"name": "", "partName": "", "targetNamespace": "", "mode": "IN", "header": False},
    "WebResult": {"name": "", "partName": "", "targetNamespace": "", "header": False},
    "SOAPBinding": {"style": "DOCUMENT", "use": "LITERAL", "parameterStyle": "WRAPPED"},
    "RequestWrapper": {"localName": "", "targetNamespace": "", "className": "", "partName": ""},
    "ResponseWrapper": {"localName": "", "targetNamespace": "", "className": "", "partName": ""},
    "Oneway": {},
}
# annotations that only matter for deployment info, never part of the JAXB/JAX-WS contract
DEPLOY_ANN = {"WebServlet", "Resource", "Override", "SuppressWarnings", "Deprecated"}
# annotations whose presence would change the wire shape in ways this tool does not model
FORBIDDEN_ANN = {"XmlAnyElement", "XmlAnyAttribute", "XmlJavaTypeAdapter", "XmlElements", "XmlElementRef",
                 "XmlElementRefs", "XmlMixed", "XmlValue", "XmlTransient", "XmlList", "XmlIDREF", "XmlID"}

SERVICES = [
    {"name": "SvcRecaudaInqV1", "sei": "SvcRecaudaInqV1PortType", "client": "SvcRecaudaInqV1",
     "impl": "SvcRecaudaInqV1_SoapHttpPortImpl", "server": "SvcRecaudaInqV1PortType_SvcRecaudaInqV1SoapHttpPort_Server"},
    {"name": "SvcRecaudaTrnV1", "sei": "SvcRecaudaTrnV1PortType", "client": "SvcRecaudaTrnV1",
     "impl": "SvcRecaudaTrnV1_SoapHttpPortImpl", "server": "SvcRecaudaTrnV1PortType_SvcRecaudaTrnV1SoapHttpPort_Server"},
]
NON_JAXB = {"CXFStartServlet", "package-info", "ObjectFactory"}


# ----------------------------------------------------------------------------------------------
# lexical helpers
# ----------------------------------------------------------------------------------------------
def read_text(path):
    raw = open(path, "rb").read()
    for enc in ("utf-8", "cp1252"):
        try:
            return raw.decode(enc)
        except UnicodeDecodeError:
            pass
    return raw.decode("latin-1")


def strip_comments(s):
    """Blank out // and /* */ comments keeping every newline (line numbers stay valid) and keeping strings."""
    out = []
    i, n = 0, len(s)
    while i < n:
        c = s[i]
        if c == '"' or c == "'":
            q = c
            j = i + 1
            while j < n and s[j] != q:
                j += 2 if s[j] == "\\" else 1
            out.append(s[i:j + 1])
            i = j + 1
        elif s.startswith("//", i):
            j = s.find("\n", i)
            j = n if j < 0 else j
            out.append(" " * (j - i))
            i = j
        elif s.startswith("/*", i):
            j = s.find("*/", i + 2)
            j = n if j < 0 else j + 2
            out.append("".join("\n" if ch == "\n" else " " for ch in s[i:j]))
            i = j
        else:
            out.append(c)
            i += 1
    return "".join(out)


def skip_string(s, i):
    q = s[i]
    j = i + 1
    while j < len(s) and s[j] != q:
        j += 2 if s[j] == "\\" else 1
    return j + 1


def read_balanced(s, i, op, cl):
    """s[i] == op; return index just after the matching cl (string aware)."""
    depth = 0
    j = i
    while j < len(s):
        c = s[j]
        if c in "\"'":
            j = skip_string(s, j)
            continue
        if c == op:
            depth += 1
        elif c == cl:
            depth -= 1
            if depth == 0:
                return j + 1
        j += 1
    raise ValueError("unbalanced " + op)


def skip_ws(s, i, end=None):
    end = len(s) if end is None else end
    while i < end and s[i].isspace():
        i += 1
    return i


def split_top(text, sep=","):
    parts, depth, cur, i = [], 0, [], 0
    while i < len(text):
        c = text[i]
        if c in "\"'":
            j = skip_string(text, i)
            cur.append(text[i:j])
            i = j
            continue
        if c in "({[<":
            depth += 1
        elif c in ")}]>":
            depth -= 1
        if c == sep and depth == 0:
            parts.append("".join(cur))
            cur = []
        else:
            cur.append(c)
        i += 1
    last = "".join(cur)
    if last.strip() != "" or parts:
        parts.append(last)
    return [p.strip() for p in parts if p.strip() != ""]


def simple(name):
    return name.strip().split(".")[-1]


def simple_type(t):
    """java.util.List<cl.x.Foo> -> List<Foo>; javax.xml.ws.Holder<X> -> Holder<X>."""
    t = re.sub(r"\s+", "", t)
    return re.sub(r"(?:[A-Za-z_][A-Za-z0-9_]*\.)+(?=[A-Za-z_])", "", t)


def parse_value(v):
    v = v.strip()
    if v.startswith("{"):
        return [parse_value(x) for x in split_top(v[1:-1])]
    if v.startswith('"'):
        parts = split_top(v, "+")
        return "".join(json.loads(p) if p.startswith('"') else "" for p in parts)
    if v in ("true", "false"):
        return v == "true"
    if re.fullmatch(r"-?\d+", v):
        return int(v)
    if v.endswith(".class"):
        return simple(v[:-6])
    return simple(v)


def read_annotations(s, i, end=None):
    end = len(s) if end is None else end
    anns = []
    while True:
        i = skip_ws(s, i, end)
        if i < end and s[i] == "@" and not s.startswith("@interface", i):
            m = re.compile(r"@\s*([\w.]+)").match(s, i)
            name = simple(m.group(1))
            j = skip_ws(s, m.end(), end)
            args = {}
            if j < end and s[j] == "(":
                k = read_balanced(s, j, "(", ")")
                body = s[j + 1:k - 1].strip()
                if body:
                    for part in split_top(body):
                        mm = re.match(r"^(\w+)\s*=(?!=)\s*(.*)$", part, re.S)
                        if mm:
                            args[mm.group(1)] = parse_value(mm.group(2))
                        else:
                            args["value"] = parse_value(part)
                j = k
            anns.append((name, args, i))
            i = j
        else:
            return anns, i


def normalize_ann(name, args, where):
    if name in FORBIDDEN_ANN:
        raise SystemExit("FORBIDDEN annotation @%s at %s: the tool does not model it" % (name, where))
    if name in DEPLOY_ANN:
        return None
    if name not in ANN_DEFAULTS:
        raise SystemExit("UNKNOWN annotation @%s at %s: extend ANN_DEFAULTS deliberately" % (name, where))
    d = ANN_DEFAULTS[name]
    for k in args:
        if k not in d:
            raise SystemExit("UNKNOWN attribute %s of @%s at %s" % (k, name, where))
    out = {}
    for k, dv in d.items():
        out[k] = args.get(k, dv)
    if name == "XmlSeeAlso" and not isinstance(out["value"], list):
        out["value"] = [out["value"]]
    return out


def norm_anns(anns, where):
    res = {}
    for name, args, _ in anns:
        n = normalize_ann(name, args, where)
        if n is not None:
            res[name] = n
    return res


# ----------------------------------------------------------------------------------------------
# Java compilation unit parser (just enough for wsimport/wsdl2java output + the *Impl classes)
# ----------------------------------------------------------------------------------------------
class Unit:
    def __init__(self, path, text):
        self.path = path
        self.raw = text
        self.src = strip_comments(text)

    def line(self, pos):
        return self.raw.count("\n", 0, pos) + 1


def parse_params(ptext, where):
    params = []
    for p in split_top(ptext):
        anns, i = read_annotations(p, 0)
        rest = p[i:].strip()
        rest = re.sub(r"^final\s+", "", rest)
        rest = re.sub(r"(\w+)\s*\[\]$", r"[] \1", rest)  # String args[] -> String[] args
        m = re.match(r"^(.*?\S(?:\.\.\.)?)\s+(\w+)$", rest, re.S)
        if not m:
            m = re.match(r"^(.*?\.\.\.)(\w+)$", rest, re.S)
        if not m:
            raise SystemExit("cannot parse parameter %r at %s" % (p, where))
        params.append({"name": m.group(2), "javaType": simple_type(m.group(1)), "annotations": norm_anns(anns, where)})
    return params


TYPE_HEAD = re.compile(r"((?:public|protected|private|static|final|abstract)\s+)*(class|interface|enum)\s+(\w+)")


def parse_type_decl(u, anns, i):
    s = u.src
    m = TYPE_HEAD.match(s, i)
    if not m:
        return None
    kind, name = m.group(2), m.group(3)
    j = m.end()
    brace = s.index("{", j)
    header = s[j:brace].strip()
    ext = re.search(r"\bextends\s+([\w.<>, ]+?)(?=\s+implements\b|$)", header)
    imp = re.search(r"\bimplements\s+([\w.<>, ]+)$", header)
    end = read_balanced(s, brace, "{", "}")
    t = {"name": name, "kind": kind, "line": u.line(m.start(2)), "file": u.path,
         "annotations": norm_anns(anns, "%s:%d" % (u.path, u.line(m.start(2)))),
         "extends": simple_type(ext.group(1)) if ext else None,
         "implements": [simple_type(x) for x in split_top(imp.group(1))] if imp else [],
         "fields": [], "methods": [], "enumConstants": [], "statics": {}, "nested": []}
    parse_body(u, t, brace + 1, end - 1)
    return t, end


def parse_body(u, t, start, end):
    s = u.src
    i = start
    if t["kind"] == "enum":
        depth, j = 0, i
        while j < end:
            c = s[j]
            if c in "\"'":
                j = skip_string(s, j)
                continue
            if c in "({":
                depth += 1
            elif c in ")}":
                depth -= 1
            elif c == ";" and depth == 0:
                break
            j += 1
        for item in split_top(s[i:j]):
            pos = s.index(item, i)
            anns, k = read_annotations(item, 0)
            mm = re.match(r"\s*(\w+)\s*(?:\((.*)\))?\s*$", item[k:], re.S)
            t["enumConstants"].append({"name": mm.group(1), "line": u.line(pos),
                                       "annotations": norm_anns(anns, "%s:%d" % (u.path, u.line(pos)))})
        i = j + 1
    while True:
        i = skip_ws(s, i, end)
        if i >= end:
            break
        if s[i] == ";":
            i += 1
            continue
        anns, i = read_annotations(s, i, end)
        hs = i
        j, depth = i, 0
        while j < end:
            c = s[j]
            if c in "\"'":
                j = skip_string(s, j)
                continue
            if c == "(":
                depth += 1
            elif c == ")":
                depth -= 1
            elif depth == 0 and c in ";{":
                break
            j += 1
        header = s[hs:j].strip()
        term = s[j] if j < end else ";"
        lead = len(s[hs:j]) - len(s[hs:j].lstrip())
        line = u.line(hs + lead)
        where = "%s:%d" % (u.path, line)
        body_end = j + 1
        if term == "{":
            body_end = read_balanced(s, j, "{", "}")
        if TYPE_HEAD.match(header + " "):
            r = parse_type_decl(u, anns, hs + lead)
            if r:
                t["nested"].append(r[0])
                i = r[1]
                continue
        eq = None
        d2 = 0
        for k, c in enumerate(header):
            if c in "(<{":
                d2 += 1
            elif c in ")>}":
                d2 -= 1
            elif c == "=" and d2 == 0:
                eq = k
                break
        if eq is not None or "(" not in header:
            decl = header if eq is None else header[:eq]
            init = None if eq is None else header[eq + 1:].strip()
            m = re.match(r"^((?:(?:public|protected|private|static|final|transient|volatile)\s+)*)(.*\S)\s+(\w+)$",
                         re.sub(r"\s+", " ", decl.strip()))
            if m and term == ";":
                mods = m.group(1).split()
                f = {"name": m.group(3), "javaType": simple_type(m.group(2)), "modifiers": mods, "line": line,
                     "annotations": norm_anns(anns, where)}
                if "static" in mods:
                    qn = re.search(r'new\s+QName\(\s*"([^"]*)"\s*,\s*"([^"]*)"\s*\)', init or "")
                    u_ = re.search(r'new\s+URL\(\s*"([^"]*)"\s*\)', init or "")
                    if qn:
                        t["statics"][f["name"]] = {"qname": [qn.group(1), qn.group(2)]}
                    elif u_:
                        t["statics"][f["name"]] = {"url": u_.group(1)}
                else:
                    t["fields"].append(f)
        else:
            m = re.match(r"^((?:(?:public|protected|private|static|final|abstract|synchronized)\s+)*)(?:(.*?)\s+)?(\w+)\s*\((.*)\)\s*(?:throws\s+(.*))?$",
                         re.sub(r"\s+", " ", header), re.S)
            if m:
                name = m.group(3)
                if name != t["name"] or m.group(2):  # skip constructors
                    t["methods"].append({
                        "name": name, "line": line, "modifiers": m.group(1).split(),
                        "returns": simple_type(m.group(2)) if m.group(2) else "void",
                        "params": parse_params(m.group(4), where),
                        "throws": [simple_type(x) for x in split_top(m.group(5) or "")],
                        "annotations": norm_anns(anns, where)})
        i = body_end


def parse_unit(path):
    u = Unit(path, read_text(path))
    s = u.src
    pkg = None
    anns, i = read_annotations(s, 0)
    m = re.compile(r"\s*package\s+([\w.]+)\s*;").match(s, i)
    pkg_anns = {}
    if m:
        pkg = m.group(1)
        pkg_anns = norm_anns(anns, path) if anns else {}
        i = m.end()
    types = []
    while True:
        i = skip_ws(s, i)
        while s.startswith("import", i):
            i = s.index(";", i) + 1
            i = skip_ws(s, i)
        if i >= len(s):
            break
        anns, i = read_annotations(s, i)
        r = parse_type_decl(u, anns, i)
        if not r:
            break
        types.append(r[0])
        i = r[1]
    return {"package": pkg, "packageAnnotations": pkg_anns, "types": types, "file": path, "unit": u}


# ----------------------------------------------------------------------------------------------
# contract assembly
# ----------------------------------------------------------------------------------------------
PRIMS = ("boolean", "byte", "short", "int", "long", "float", "double", "char")


def prop_view(f, pkg_ns, qualified):
    el = f["annotations"].get("XmlElement")
    at = f["annotations"].get("XmlAttribute")
    jt = f["javaType"]
    coll = None
    inner = jt
    m = re.match(r"^(List|Collection|Set)<(.*)>$", jt)
    if m:
        coll, inner = m.group(1), m.group(2)
    primitive = inner in PRIMS
    extra = set(f["annotations"]) - {"XmlElement", "XmlAttribute", "XmlSchemaType"}
    if extra:
        raise SystemExit("property %s has unmodelled annotations %s" % (f["name"], sorted(extra)))
    p = {"field": f["name"], "javaType": inner, "collection": coll, "primitive": primitive,
         "line": f["line"], "annotations": {k: v for k, v in sorted(f["annotations"].items())}}
    if el is not None:
        name = f["name"] if el["name"] == "##default" else el["name"]
        ns = el["namespace"]
        p["kind"] = "element"
        p["xmlName"] = name
        p["effectiveNamespace"] = (pkg_ns if qualified else "") if ns == "##default" else ns
        req = el["required"]
        if coll:
            p["cardinality"] = "1..n" if req else "0..n"
        elif primitive:
            p["cardinality"] = "1..1"
        else:
            p["cardinality"] = "1..1" if req else "0..1"
    elif at is not None:
        p["kind"] = "attribute"
        p["xmlName"] = f["name"] if at["name"] == "##default" else at["name"]
        p["effectiveNamespace"] = "" if at["namespace"] == "##default" else at["namespace"]
        p["cardinality"] = "1..1" if at["required"] else "0..1"
    else:
        p["kind"] = "unannotated"  # FIELD access: JAXB maps it as optional element named like the field
        p["xmlName"] = f["name"]
        p["effectiveNamespace"] = pkg_ns if qualified else ""
        p["cardinality"] = "0..n" if coll else ("1..1" if primitive else "0..1")
    return p


def build(legacy_dir, legacy_root):
    files = sorted(f for f in os.listdir(legacy_dir) if f.endswith(".java"))
    units = {f[:-5]: parse_unit(os.path.join(legacy_dir, f)) for f in files}

    def rel(p):
        return os.path.relpath(p, legacy_root).replace("\\", "/") if legacy_root else os.path.basename(p)

    pkg_unit = units["package-info"]
    xs = pkg_unit["packageAnnotations"].get("XmlSchema")
    if not xs:
        raise SystemExit("package-info without @XmlSchema")
    pkg_ns = xs["namespace"]
    qualified = xs["elementFormDefault"] == "QUALIFIED"
    package = {"name": units["ObjectFactory"]["package"], "namespace": pkg_ns,
               "elementFormDefault": xs["elementFormDefault"], "attributeFormDefault": xs["attributeFormDefault"],
               "annotations": {"XmlSchema": xs}, "source": {"file": rel(pkg_unit["file"]), "line": 1}}

    service_names = set()
    for sv in SERVICES:
        service_names.update([sv["sei"], sv["client"], sv["impl"], sv["server"]])
    jaxb = {}
    for name, cu in units.items():
        if name in NON_JAXB or name in service_names:
            continue
        for t in cu["types"]:
            if "XmlType" in t["annotations"]:
                jaxb[t["name"]] = (t, cu)
            else:
                raise SystemExit("type %s has no @XmlType and is not a known service class" % t["name"])
    of_t = units["ObjectFactory"]["types"][0]
    objfac = {"class": "ObjectFactory", "source": {"file": rel(units["ObjectFactory"]["file"]), "line": of_t["line"]},
              "annotations": {k: v for k, v in sorted(of_t["annotations"].items())},
              "elementDecls": [], "createMethods": []}
    for m in of_t["methods"]:
        if "XmlElementDecl" in m["annotations"]:
            d = m["annotations"]["XmlElementDecl"]
            rt = re.match(r"^JAXBElement<(.*)>$", m["returns"])
            objfac["elementDecls"].append({
                "method": m["name"], "namespace": d["namespace"], "name": d["name"], "scope": d["scope"],
                "substitutionHeadName": d["substitutionHeadName"],
                "valueType": rt.group(1) if rt else m["returns"],
                "paramTypes": [p["javaType"] for p in m["params"]], "line": m["line"]})
        elif m["name"].startswith("create"):
            objfac["createMethods"].append({"method": m["name"], "returns": m["returns"], "line": m["line"]})
    objfac["elementDecls"].sort(key=lambda d: (d["name"], d["method"]))
    objfac["createMethods"].sort(key=lambda d: d["method"])

    def type_view(t, cu):
        xt = t["annotations"].get("XmlType")
        v = {"name": t["name"], "kind": t["kind"], "source": {"file": rel(cu["file"]), "line": t["line"]},
             "extends": t["extends"],
             "annotations": {k: val for k, val in sorted(t["annotations"].items())}}
        if t["kind"] == "enum":
            vals = []
            for c in t["enumConstants"]:
                ev = c["annotations"].get("XmlEnumValue")
                vals.append({"constant": c["name"], "xmlValue": ev["value"] if ev else c["name"], "line": c["line"]})
            v["enumValues"] = vals
            v["properties"] = []
        else:
            v["properties"] = [prop_view(f, pkg_ns, qualified) for f in t["fields"]]
            declared = {p["field"] for p in v["properties"]}
            order = [x for x in xt["propOrder"] if x != ""]
            if order and set(order) != declared:
                raise SystemExit("%s propOrder %s != fields %s" % (t["name"], order, sorted(declared)))
            v["enumValues"] = []
        v["effectiveXmlTypeNamespace"] = pkg_ns if xt["namespace"] == "##default" else xt["namespace"]
        return v

    all_types = {n: type_view(t, cu) for n, (t, cu) in jaxb.items()}

    def refs(tv):
        r = set(p["javaType"] for p in tv["properties"])
        if tv["extends"]:
            r.add(tv["extends"])
        r.update(tv["annotations"].get("XmlSeeAlso", {}).get("value", []))
        return {x for x in r if x in all_types}

    def closure(roots):
        seen, stack = set(), [x for x in roots if x in all_types]
        while stack:
            x = stack.pop()
            if x in seen:
                continue
            seen.add(x)
            stack.extend(refs(all_types[x]))
        return seen

    of_roots = {d["valueType"] for d in objfac["elementDecls"]} | {c["returns"] for c in objfac["createMethods"]}

    contracts = {}
    for sv in SERVICES:
        sei_t = units[sv["sei"]]["types"][0]
        cli_t = units[sv["client"]]["types"][0]
        imp_t = units[sv["impl"]]["types"][0]
        srv_u = units[sv["server"]]
        sei_view = {"class": sei_t["name"], "kind": sei_t["kind"],
                    "source": {"file": rel(units[sv["sei"]]["file"]), "line": sei_t["line"]},
                    "annotations": {k: v for k, v in sorted(sei_t["annotations"].items())}, "operations": []}
        op_param_types = set()
        for m in sei_t["methods"]:
            ps = []
            for p in m["params"]:
                h = re.match(r"^Holder<(.*)>$", p["javaType"])
                op_param_types.add(h.group(1) if h else p["javaType"])
                ps.append({"name": p["name"], "javaType": p["javaType"], "holder": bool(h),
                           "valueType": h.group(1) if h else p["javaType"], "annotations": p["annotations"]})
            sei_view["operations"].append({
                "javaMethod": m["name"], "returns": m["returns"], "throws": m["throws"], "params": ps,
                "annotations": {k: v for k, v in sorted(m["annotations"].items())},
                "source": {"file": rel(units[sv["sei"]]["file"]), "line": m["line"]}})
        sei_view["operations"].sort(key=lambda o: o["javaMethod"])
        client_view = {"class": cli_t["name"], "extends": cli_t["extends"],
                       "source": {"file": rel(units[sv["client"]]["file"]), "line": cli_t["line"]},
                       "annotations": {k: v for k, v in sorted(cli_t["annotations"].items())},
                       "qnames": {k: v["qname"] for k, v in sorted(cli_t["statics"].items()) if "qname" in v},
                       "webEndpoints": sorted(
                           [{"javaMethod": m["name"], "returns": m["returns"], "paramCount": len(m["params"]),
                             "name": m["annotations"]["WebEndpoint"]["name"], "line": m["line"]}
                            for m in cli_t["methods"] if "WebEndpoint" in m["annotations"]],
                           key=lambda e: (e["javaMethod"], e["paramCount"])),
                       "defaultWsdlUrl": (re.findall(r'new\s+URL\(\s*"([^"]*)"\s*\)', units[sv["client"]]["unit"].src) or [None])[0]}
        impl_view = {"class": imp_t["name"], "implements": imp_t["implements"],
                     "source": {"file": rel(units[sv["impl"]]["file"]), "line": imp_t["line"]},
                     "annotations": {k: v for k, v in sorted(imp_t["annotations"].items())}}
        notes = {
            "publicMethods": sorted(
                [{"name": m["name"], "returns": m["returns"], "params": [p["javaType"] for p in m["params"]],
                  "throws": m["throws"], "line": m["line"]}
                 for m in imp_t["methods"] if "public" in m["modifiers"]], key=lambda x: (x["name"], x["line"])),
            "nestedTypes": sorted(
                [{"name": n["name"], "kind": n["kind"], "constants": [c["name"] for c in n["enumConstants"]]}
                 for n in imp_t["nested"]], key=lambda x: x["name"]),
            "note": "Informational, NOT compared by SoapContratoEstaticoTest (behavior, not contract)."}
        addr = re.findall(r'String\s+address\s*=\s*"([^"]*)"', srv_u["unit"].src)
        deploy = {"serverMainPublishAddress": addr,
                  "serverSource": {"file": rel(srv_u["file"])},
                  "servlet": {"class": "CXFStartServlet",
                              "urlPatterns": re.findall(r'urlPatterns\s*=\s*"([^"]*)"', units["CXFStartServlet"]["unit"].src),
                              "source": {"file": rel(units["CXFStartServlet"]["file"])}}}
        in_closure = closure(op_param_types | of_roots)
        from_ops = closure(op_param_types)
        types = []
        for n in sorted(in_closure):
            tv = dict(all_types[n])
            tv["reachableFromOperationParams"] = n in from_ops
            types.append(tv)
        contracts[sv["name"]] = {
            "contractVersion": 1,
            "service": sv["name"],
            "extraction": {"tool": "soap_contract_extract.py", "mode": "static-text-parse",
                           "proves": "annotation-level contract as written in the legacy sources",
                           "doesNotProve": ["real WSDL", "wire XML", "consumer (BCP) validation"]},
            "package": package,
            "endpointImplementation": impl_view,
            "portType": sei_view,
            "serviceClient": client_view,
            "objectFactory": objfac,
            "types": types,
            "deployment": deploy,
            "legacyImplementationNotes": notes,
            "stats": {"typeCount": len(types), "classes": sum(1 for t in types if t["kind"] == "class"),
                      "enums": sum(1 for t in types if t["kind"] == "enum"),
                      "elementDecls": len(objfac["elementDecls"]),
                      "collectionProperties": sum(1 for t in types for p in t["properties"] if p["collection"]),
                      "notReachableFromOperationParams": sorted(n for n in in_closure if n not in from_ops)},
        }
    covered = set()
    for c in contracts.values():
        covered |= {t["name"] for t in c["types"]}
    return contracts, sorted(set(all_types) - covered)


def summary_md(contracts, orphan):
    L = ["# SOAP contract summary (generated by soap_contract_extract.py - do not edit)", "",
         "Static capture of the legacy sources. NOT a WSDL, NOT wire XML, NOT consumer-validated (see README.md).", ""]
    for name, c in sorted(contracts.items()):
        L += ["## " + name, ""]
        ei = c["endpointImplementation"]
        w = ei["annotations"]["WebService"]
        L += ["| item | value | legacy source |", "|---|---|---|"]
        L.append("| endpoint class | %s implements %s | %s:%d |" % (
            ei["class"], ", ".join(ei["implements"]), ei["source"]["file"], ei["source"]["line"]))
        L.append("| @WebService (impl) | serviceName=%s portName=%s targetNamespace=%s endpointInterface=%s | same |" % (
            w["serviceName"], w["portName"], w["targetNamespace"], w["endpointInterface"]))
        sw = c["portType"]["annotations"]["WebService"]
        L.append("| @WebService (SEI) | name=%s targetNamespace=%s | %s:%d |" % (
            sw["name"], sw["targetNamespace"], c["portType"]["source"]["file"], c["portType"]["source"]["line"]))
        sb = c["portType"]["annotations"].get("SOAPBinding")
        if sb:
            L.append("| @SOAPBinding | style=%s use=%s parameterStyle=%s | same |" % (sb["style"], sb["use"], sb["parameterStyle"]))
        sc = c["serviceClient"]["annotations"]["WebServiceClient"]
        L.append("| @WebServiceClient | name=%s targetNamespace=%s wsdlLocation=%s | %s:%d |" % (
            sc["name"], sc["targetNamespace"], sc["wsdlLocation"], c["serviceClient"]["source"]["file"],
            c["serviceClient"]["source"]["line"]))
        L.append("| package | namespace=%s elementFormDefault=%s | %s |" % (
            c["package"]["namespace"], c["package"]["elementFormDefault"], c["package"]["source"]["file"]))
        L.append("| servlet urlPatterns | %s | %s |" % (", ".join(c["deployment"]["servlet"]["urlPatterns"]), c["deployment"]["servlet"]["source"]["file"]))
        L.append("| Endpoint.publish address (dev main) | %s | %s |" % (", ".join(c["deployment"]["serverMainPublishAddress"]), c["deployment"]["serverSource"]["file"]))
        L += ["", "### Operations", "",
              "| javaMethod | operationName | action | param | mode | partName | name | targetNamespace | Java type |",
              "|---|---|---|---|---|---|---|---|---|"]
        for o in c["portType"]["operations"]:
            for p in o["params"]:
                wp = p["annotations"].get("WebParam", {})
                L.append("| %s | %s | %s | %s | %s | %s | %s | %s | %s |" % (
                    o["javaMethod"], o["annotations"]["WebMethod"]["operationName"], o["annotations"]["WebMethod"]["action"],
                    p["name"], wp.get("mode"), wp.get("partName"), wp.get("name"), wp.get("targetNamespace"), p["javaType"]))
        L += ["", "### Types (%d: %d classes, %d enums; %d ObjectFactory element decls)" % (
            c["stats"]["typeCount"], c["stats"]["classes"], c["stats"]["enums"], c["stats"]["elementDecls"]), "",
            "| type | kind | xml type name | properties in propOrder (xml name:type [cardinality]) | lists | enum values |",
            "|---|---|---|---|---|---|"]
        for t in c["types"]:
            xt = t["annotations"].get("XmlType", {})
            byf = {p["field"]: p for p in t["properties"]}
            order = [x for x in xt.get("propOrder", []) if x]
            plist = ", ".join("%s:%s%s [%s]" % (byf[f]["xmlName"], byf[f]["javaType"], "[]" if byf[f]["collection"] else "",
                                                byf[f]["cardinality"]) for f in order)
            lists = sum(1 for p in t["properties"] if p["collection"])
            ev = ", ".join(v["xmlValue"] for v in t["enumValues"])
            L.append("| %s | %s | %s | %s | %d | %s |" % (t["name"], t["kind"], xt.get("name"), plist, lists, ev))
        L.append("")
    if orphan:
        L += ["Types not reachable from any service: " + ", ".join(orphan), ""]
    return "\n".join(L)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--legacy-dir", required=True)
    ap.add_argument("--out-dir", required=True)
    ap.add_argument("--legacy-root", default=None, help="repo root used to make source paths relative")
    a = ap.parse_args()
    contracts, orphan = build(a.legacy_dir, a.legacy_root)
    os.makedirs(a.out_dir, exist_ok=True)
    for name, c in contracts.items():
        with open(os.path.join(a.out_dir, name + ".contract.json"), "w", encoding="utf-8", newline="\n") as f:
            json.dump(c, f, indent=2, sort_keys=True, ensure_ascii=False)
            f.write("\n")
    with open(os.path.join(a.out_dir, "CONTRACT-SUMMARY.md"), "w", encoding="utf-8", newline="\n") as f:
        f.write(summary_md(contracts, orphan) + "\n")
    for name, c in sorted(contracts.items()):
        print("%s: %d types (%d classes, %d enums), %d ops, %d element decls, %d list props, not-from-ops=%s" % (
            name, c["stats"]["typeCount"], c["stats"]["classes"], c["stats"]["enums"],
            len(c["portType"]["operations"]), c["stats"]["elementDecls"], c["stats"]["collectionProperties"],
            c["stats"]["notReachableFromOperationParams"]))
    if orphan:
        print("WARNING orphan types: %s" % orphan, file=sys.stderr)


if __name__ == "__main__":
    main()
