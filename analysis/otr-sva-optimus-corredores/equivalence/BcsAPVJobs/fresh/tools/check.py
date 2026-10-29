import glob, os, sys, hashlib
fresh = sys.argv[1]
dev = sys.argv[2]
devh = {}
for p in glob.glob(os.path.join(dev, 'legacy', 'apv-jobs', '*.txt')):
    devh.setdefault(hashlib.sha256(open(p, 'rb').read()).hexdigest(), []).append(os.path.basename(p))
byc = {}
for p in sorted(glob.glob(os.path.join(fresh, 'legacy', 'apv-jobs', '*.txt'))):
    b = open(p, 'rb').read()
    n = os.path.basename(p)
    cid = n.split('__')[0]
    facet = n.split('__')[1]
    h = hashlib.sha256(b).hexdigest()
    same = devh.get(h, [])
    byc.setdefault(cid, []).append((facet, len(b), same))
for cid, fs in byc.items():
    nonempty = [f for f in fs if f[1] > 4]
    dup = [(f[0], f[2][0]) for f in fs if f[2] and f[1] > 4]
    print(cid[:22], 'bytes', [f[1] for f in fs], 'identical-to-dev facets:', dup)
