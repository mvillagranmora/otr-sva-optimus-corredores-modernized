INSERT INTO "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento" (
"IdDefinicionAsiento",
"IdTipoContabilidad",
"IdCuentaContable",
"Ejercicio",
"Orden",
"DebeHaber",
"TipoEvento",
"Condicionado",
"OmiteCondicionCmbte",
"Estado",
"Formato")
VALUES (
:IdDefinicionAsiento,
:IdTipoContabilidad,
:IdCuentaContable,
:Ejercicio,
:Orden,
:DebeHaber,
:TipoEvento,
:Condicionado,
:OmiteCondicionCmbte,
:Estado,
:Formato);