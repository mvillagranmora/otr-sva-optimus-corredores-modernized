import sys, os, shutil
# usage: reencode.py <dest> <utilsRoot> <plaRoot> <ctaRoot> utilsNames... -- plaRelNames... -- ctaNames...
dest, utils, pla, cta = sys.argv[1:5]
rest = sys.argv[5:]
groups = [[]]
for a in rest:
    if a == '--':
        groups.append([])
    else:
        groups[-1].append(a)
jobs = []
for n in groups[0]:
    jobs.append((utils + '/' + n + '.java', 'cl/bcs/utils/' + n + '.java'))
for n in groups[1]:
    jobs.append((pla + '/' + n + '.java', 'cl/bcs/plataforma/' + n + '.java'))
for n in (groups[2] if len(groups) > 2 else []):
    jobs.append((cta + '/' + n + '.java', 'cl/bcs/cuentaInversion/' + n + '.java'))
for a in (groups[3] if len(groups) > 3 else []):
    x, y = a.split('|')
    jobs.append((x, y))
for src, rel in jobs:
    if not os.path.exists(src):
        print('MISSING', src)
        sys.exit(1)
    b = open(src, 'rb').read()
    try:
        t = b.decode('utf-8')
    except UnicodeDecodeError:
        t = b.decode('cp1252', errors='replace')
    if rel.startswith('cl/bcs/cuentaInversion/'):
        # POI compatibility (POI <= 3.16 short constants -> POI 3.17 enums); STYLING ONLY, no cell value is touched
        for a, b in [('CellStyle.BORDER_THIN', 'org.apache.poi.ss.usermodel.BorderStyle.THIN'),
                     ('CellStyle.ALIGN_CENTER', 'org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER'),
                     ('CellStyle.VERTICAL_CENTER', 'org.apache.poi.ss.usermodel.VerticalAlignment.CENTER'),
                     ('HSSFCellStyle.SOLID_FOREGROUND', 'org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND'),
                     ('CellStyle.SOLID_FOREGROUND', 'org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND')]:
            t = t.replace(a, b) if a != 'CellStyle.SOLID_FOREGROUND' else t.replace(' CellStyle.SOLID_FOREGROUND', ' ' + b).replace('(CellStyle.SOLID_FOREGROUND', '(' + b)
    out = os.path.join(dest, rel)
    os.makedirs(os.path.dirname(out), exist_ok=True)
    open(out, 'w', encoding='utf-8', newline='').write(t)
print('copied', len(jobs))
