#!/usr/bin/env python3
"""Generates the REGISTERED DOUBLES of the oracle from the REAL legacy sources (never edits legacy/): the double keeps every declaration of
the original (imports, fields, nested public types, constructors) and replaces the BODY of every method declared directly in the top level
class: `logica` (and `go`, `call`) -> cl.bcs.oracle.Doubles (canned answer configured per case + record of the call); any other method ->
throws UnsupportedOperationException. Result: same API as the real collaborator (so the closure compiles unchanged), behaviour = script.

  py -3 -I gen_doubles.py <outDir> <legacySrcFile> [<legacySrcFile> ...]      (package/path of each double is taken from its `package` line)
"""
import os
import re
import sys

ENTRY = {"logica", "go", "call"}


def mask(src):
    """Same-length copy of src where comments, string and char literals are blanked (so braces/parens can be matched safely)."""
    out, i, n = [], 0, len(src)
    while i < n:
        c = src[i]
        if src.startswith("//", i):
            j = src.find("\n", i)
            j = n if j < 0 else j
            out.append(" " * (j - i)); i = j
        elif src.startswith("/*", i):
            j = src.find("*/", i + 2)
            j = n if j < 0 else j + 2
            out.append("".join(ch if ch == "\n" else " " for ch in src[i:j])); i = j
        elif src.startswith('"""', i):
            j = src.find('"""', i + 3) + 3
            out.append("".join(ch if ch == "\n" else " " for ch in src[i:j])); i = j
        elif c in "\"'":
            j = i + 1
            while j < n and src[j] != c:
                j += 2 if src[j] == "\\" else 1
            j += 1
            out.append(c + " " * (j - i - 2) + c); i = j
        else:
            out.append(c); i += 1
    return "".join(out)


def match(m, i):
    """index of the '}' closing the '{' at m[i]"""
    d = 0
    for j in range(i, len(m)):
        if m[j] == "{": d += 1
        elif m[j] == "}":
            d -= 1
            if d == 0: return j
    raise ValueError("unbalanced")


def split_params(p):
    parts, d, cur = [], 0, ""
    for ch in p:
        if ch in "<(": d += 1
        if ch in ">)": d -= 1
        if ch == "," and d == 0: parts.append(cur); cur = ""
        else: cur += ch
    if cur.strip(): parts.append(cur)
    return [x.strip() for x in parts]


def transform(src, simple):
    m = mask(src)
    start = re.search(r"\b(class|interface)\s+" + simple + r"\b[^{]*\{", m)
    top_open = start.end() - 1
    top_close = match(m, top_open)
    edits, seg, i = [], top_open + 1, top_open + 1
    while i < top_close:
        ch = m[i]
        if ch == ";":
            if re.search(r"\bprivate\b", m[seg:i]) and "(" not in m[seg:i].split("=")[0]:
                edits.append((seg, i + 1, ""))      # private field: not part of the double
            seg = i + 1
        elif ch == "{":
            end = match(m, i)
            header = re.sub(r"@\w+(\([^)]*\))?", " ", m[seg:i]).strip()
            h = header
            if re.search(r"\bprivate\b", h) and "(" in h and "=" not in h and not re.search(r"\b(class|interface|enum|record)\b", h):
                edits.append((seg, end + 1, ""))        # private method: dropped (its signature may name types we do not compile)
            elif re.search(r"\b(class|interface|enum|record)\b", h) or "=" in h or not h.endswith((")",)) and "throws" not in h:
                pass                                   # nested type, field initializer, static/instance init block: keep verbatim
            else:
                name = re.search(r"([A-Za-z_]\w*)\s*\(", h).group(1)
                pre = h[:h.index("(")].split()
                ret = pre[-2] if len(pre) >= 2 else ""
                params = [re.findall(r"[A-Za-z_]\w*", p)[-1] for p in split_params(h[h.index("(") + 1:h.rindex(")")] if ")" in h else "")]
                if name == simple:
                    body = "{ }"
                elif name in ENTRY:
                    call = 'cl.bcs.oracle.Doubles.answer("%s", "%s", new Object[]{%s}, %s)' % (simple, name, ", ".join(params), (ret + ".class") if ret not in ("void", "") else "Void.class")
                    body = "{ %s; }" % call if ret == "void" else "{ return %s; }" % call
                else:
                    body = '{ throw new UnsupportedOperationException("oracle double %s.%s: not part of the double"); }' % (simple, name)
                edits.append((i, end + 1, body))
            i = end; seg = end + 1
        elif ch == "}": seg = i + 1
        i += 1
    for a, b, body in reversed(edits):
        src = src[:a] + body + src[b:]
    return prune_imports(src)


def prune_imports(src):
    """drop `import x.Y;` whose simple name is no longer used once the bodies are gone (the removed bodies were the only users)"""
    m = mask(src)
    body = re.sub(r"^\s*import\s[^;]+;", "", m, flags=re.M)
    keep = []
    for line in src.split("\n"):
        mm = re.match(r"\s*import\s+(?!static)([\w.]+)\.(\w+)\s*;", line)
        if mm and re.search(r"\bclass\s+" + mm.group(2) + r"\b", body):
            continue                                    # the file declares its own nested class of that name: the import was dead
        if mm and not re.search(r"\b" + mm.group(2) + r"\b", body):
            continue
        keep.append(line)
    return "\n".join(keep)


def main():
    out = sys.argv[1]
    for f in sys.argv[2:]:
        src = open(f, encoding="utf-8", errors="replace").read()
        pkg = re.search(r"^\s*package\s+([\w.]+)\s*;", src, re.M).group(1)
        simple = os.path.splitext(os.path.basename(f))[0]
        d = os.path.join(out, *pkg.split("."))
        os.makedirs(d, exist_ok=True)
        text = "// GENERATED DOUBLE of the legacy %s.%s by gen_doubles.py (method bodies replaced; declarations are the legacy ones)\n" % (pkg, simple)
        with open(os.path.join(d, simple + ".java"), "w", encoding="utf-8", newline="\n") as fh:
            fh.write(text + transform(src, simple))
        print("double:", pkg + "." + simple)


if __name__ == "__main__":
    main()
