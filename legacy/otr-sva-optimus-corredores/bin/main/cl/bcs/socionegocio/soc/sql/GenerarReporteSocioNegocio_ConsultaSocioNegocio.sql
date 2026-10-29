SELECT 
TO_CHAR("FechaIngreso",'DD-MM-YYYY') AS "FechaIngreso"
,"NombreTipoDocumentoIdentidadDefecto"
,"DocumentoIdentidadDefecto"
,"TipoPersona"
,"NombreCompleto"
,"NombrePaisResidencia"
,CASE "EsCliente" WHEN 'UI_SI' THEN 'SI' ELSE 'NO' END AS "EsCliente"
,CASE "EsEmpleado" WHEN 'UI_SI' THEN 'SI' ELSE 'NO' END AS "EsEmpleado"
,CASE "EsProveedor" WHEN 'UI_SI' THEN 'SI' ELSE 'NO' END AS "EsProveedor"
,CASE "EsContraparte" WHEN 'UI_SI' THEN 'SI' ELSE 'NO' END AS "EsContraparte"
,"DescripcionSegmento"
FROM "BSCL.SocioNegocio.BD.Vistas::SOC.UIBlotterConsultarSocioDeNegocio"
WHERE
"IdEmpresa" = :IdEmpresa
AND (:EsCliente = '' OR "EsCliente" = :EsCliente)
AND (:EsProveedor = '' OR "EsProveedor" = :EsProveedor)
AND (:EsEmpleado = '' OR "EsEmpleado" = :EsEmpleado)
AND (:EsContraparte = '' OR "EsContraparte" = :EsContraparte)
AND (:FechaDesde = '0001-01-01' OR TO_DATE("FechaIngreso") >= :FechaDesde)
AND (:FechaHasta = '0001-01-01' OR TO_DATE("FechaIngreso") <= :FechaHasta)
AND (:ListaDocumentosIdentidad = '' OR UPPER("ListaDocumentosIdentidad") LIKE :ListaDocumentosIdentidad)
AND (:IdTipoDocumentoIdentidadDefecto = '' OR "IdTipoDocumentoIdentidadDefecto" = :IdTipoDocumentoIdentidadDefecto)
AND ((:Alias = '' OR UPPER ("Alias") LIKE :Alias) OR (:NombreCompleto = '' OR UPPER("NombreCompleto") LIKE :NombreCompleto))
