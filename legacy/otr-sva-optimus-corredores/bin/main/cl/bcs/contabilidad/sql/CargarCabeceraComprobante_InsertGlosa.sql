INSERT INTO "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoGlosa" (
"IdTipoContabilidad",
"IdDefinicionAsiento",
"Origen",
"Orden",
"GlosaCampo",
"NombreCampoMovimiento",
"IdTipoDatoEvento",
"Estado")
 VALUES (
 :IdTipoContabilidad,
 :IdDefinicionAsiento,
 :Origen,
 :Orden,
 :GlosaCampo,
 :NombreCampoMovimiento,
 :IdTipoDatoEvento,
 :Estado);