SELECT
	DOC."Version" AS "Periodo",
	TO_VARCHAR(ESQ."FechaDatosDesde",'YYYY-MM-DD') AS "FechaDesde",
	TO_VARCHAR(ESQ."FechaDatosHasta",'YYYY-MM-DD') AS "FechaHasta"
FROM "BSCL.Normativa.BD::DOC.Documento" DOC
LEFT JOIN "BSCL.Normativa.BD::DOC.Esquema" ESQ
	ON DOC."Esquema"=ESQ."Esquema" AND DOC."Version"=ESQ."Version"
WHERE DOC."Esquema"='DJ1' AND DOC."IdDocumento"=:idDocumento;