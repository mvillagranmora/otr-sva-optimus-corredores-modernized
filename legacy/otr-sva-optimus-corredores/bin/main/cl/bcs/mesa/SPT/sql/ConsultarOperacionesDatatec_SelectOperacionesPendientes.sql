SELECT OSD."IdDatatec",
	OSD."NumeroRegistro",
	OSD."FechaOperacion",
	TIPO."Nombre" AS "TipoOperacion",
	OSD."GlosaProceso",
	EST."Nombre" AS "Estado",
	OSD."NombreCliente",
	OSD."IdMonedaPrincipal",
	OSD."IdMonedaSecundaria",
	OSD."MontoMonedaPrincipal",
	OSD."TipoCambioCierre",
	OSD."MontoMonedaSecundaria"
FROM "BSCL.Mesa.BD::SPT.OperacionSpotDatatec" AS OSD
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPO ON OSD."IdTipoOperacion"=TIPO."IdTipoOperacion"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST ON OSD."IdEstadoRegistro"=EST."IdEstado"
WHERE OSD."FechaOperacion" LIKE '%'||:p_Fecha||'%'
AND OSD."IdEstadoRegistro" = 'PEN' 
AND OSD."IdEmpresa" = :p_IdEmpresa;