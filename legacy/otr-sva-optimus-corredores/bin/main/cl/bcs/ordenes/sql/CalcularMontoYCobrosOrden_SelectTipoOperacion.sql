SELECT "IdTipoOperacion","AccionOrden"
FROM "BSCL.Plataforma.BD::INS.TipoOperacion" ope
WHERE "IdTipoOperacion" in (:idsTipoOperacion);
