WITH Datos AS (
	SELECT
		 CAR."IdTipoInstrumento"
		 ,'CLP' AS "IdMonedaInstrumento"
		 ,CAR."FechaVencimientoInstrumento"
		 ,CASE WHEN :TipoValorizacion = 'C' THEN
		 CAR."INGMontoATasaCompraActual"
		 ELSE
		 CAR."INGMontoATasaMercadoActual"
		 END AS "MontoValorActual"
		 ,'CD' AS "Diferenciador"--,RES."IdOperacion"
		 FROM "BSCL.Custodia.BD::CUS.Reserva" AS RES
		 LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS  CAR ON RES."IdCarteraCustodia" = CAR."IdCartera"
		 LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" AS SUB ON SUB."FolioSubyacente" = RES."FolioSubyacente"
		 LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF
		 ON MF."IdTipoInstrumento" = CAR."IdTipoInstrumento"
		 AND MF."IdMoneda" = CAR."IdMonedaInstrumento"
		 AND MF."IdUsoMonedaFormato" = 'GEN'
		 AND MF."IdTipoOperacion" = ''
		 INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = CAR."IdCliente"
		 INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersona"
		 WHERE SUB."IdEstado" = 'VIG'
		 AND   CAR."IdCliente"= :IdCliente
		 AND   CAR."IdPortafolio" = CASE WHEN :IdPortafolio = 0 THEN CAR."IdPortafolio" ELSE :IdPortafolio END
		 AND   CAR."IdTipoOperacion"='COMPAC'
		 AND   CAR."TipoCarteraCorredor"='CC'
		 AND   CAR."FechaOperacion" <= :Fecha
	 UNION ALL
	 SELECT
		 CAR."IdTipoInstrumento"
		 ,'CLP' AS "IdMonedaInstrumento"
		 ,CAR."FechaVencimientoInstrumento"
		 ,CASE WHEN :TipoValorizacion = 'C' THEN
		 CAR."INGMontoATasaCompraActual"
		 ELSE
		 CAR."INGMontoATasaMercadoActual"
		 END AS "MontoValorActual"
		 ,'CP' AS "Diferenciador"--,RES."IdOperacion"
		 FROM "BSCL.Custodia.BD::CUS.Reserva" AS RES
		 INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON RES."IdCarteraCustodia" = CAR."IdCartera"
		 INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" AS SUB ON SUB."FolioSubyacente" = RES."FolioSubyacente"
		 LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF
		 ON MF."IdTipoInstrumento" = CAR."IdTipoInstrumento"
		 AND MF."IdMoneda" = CAR."IdMonedaInstrumento"
		 AND MF."IdUsoMonedaFormato" = 'GEN'
		 AND MF."IdTipoOperacion" = ''
		 INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = CAR."IdCliente"
		 INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersona"
		 WHERE CAR."IdEstado" = 'VIG'
		 AND   CAR."IdClienteRelacionado"= :IdCliente
		 AND   CAR."IdPortafolioRelacionado" = CASE WHEN :IdPortafolio = 0 THEN CAR."IdPortafolioRelacionado" ELSE :IdPortafolio END
		 AND   CAR."IdTipoOperacion"='VENPAC'
		 --AND   CAR."TipoCarteraCorredor" <>'CC' --confirmacion carga de datos
		 AND   CAR."FechaOperacion" <= :Fecha
),
CantidadDatos AS (
	SELECT COUNT(*) AS "TotalDatos", 
		"IdTipoInstrumento",
		"IdMonedaInstrumento",
		"FechaVencimientoInstrumento",
		"MontoValorActual",
		"Diferenciador"
		FROM Datos
		GROUP BY 
	        "IdTipoInstrumento", 
	        "IdMonedaInstrumento", 
	        "FechaVencimientoInstrumento", 
	        "MontoValorActual",
	        "Diferenciador"
)
SELECT
 T."IdTipoInstrumento"
 ,T."Nombre" AS "NombreTipoInstrumento"
 ,CASE
        WHEN C."TotalDatos" != 0 THEN 'CLP'
        ELSE ''
    END AS "IdMonedaInstrumento"
 --Cartera Libre
 ,SUM(CASE WHEN DAYS_BETWEEN (TO_DATE (C."FechaVencimientoInstrumento", 'YYYY-MM-DD'), TO_DATE(:FechaSistema, 'YYYY-MM-DD'))  >7 AND C."Diferenciador"='CD'
 THEN COALESCE(C."MontoValorActual",0) ELSE 0 END) AS "MontoValorActualMayorCD"
 ,SUM(CASE WHEN DAYS_BETWEEN (TO_DATE (C."FechaVencimientoInstrumento", 'YYYY-MM-DD'), TO_DATE(:FechaSistema, 'YYYY-MM-DD')) <=7  AND C."Diferenciador"='CD'
 THEN COALESCE(C."MontoValorActual",0) ELSE 0 END) AS "MontoValorActualMenorCD"
 --Cartera Pacteada
 ,SUM(CASE WHEN DAYS_BETWEEN (TO_DATE (C."FechaVencimientoInstrumento", 'YYYY-MM-DD'), TO_DATE(:FechaSistema, 'YYYY-MM-DD')) >7 AND C."Diferenciador"='CP'
 THEN COALESCE(C."MontoValorActual",0) ELSE 0 END) AS "MontoValorActualMayorCP"
 ,SUM(CASE WHEN DAYS_BETWEEN (TO_DATE (C."FechaVencimientoInstrumento", 'YYYY-MM-DD'), TO_DATE(:FechaSistema, 'YYYY-MM-DD')) <=7 AND C."Diferenciador"='CP'
 THEN COALESCE(C."MontoValorActual",0) ELSE 0 END) AS "MontoValorActualMenorCP"
 FROM "BSCL.Plataforma.BD::INS.TipoInstrumento" AS T
 LEFT JOIN  CantidadDatos AS C ON (C."IdTipoInstrumento"=T."IdTipoInstrumento")
 WHERE T."IdTipoInstrumento" IN ('BCD','BCP','BCU','BTP','BTU','BCX','ZER','CER','XER','PRC','PCX','PRD','BBC','LTH','BCA','BR','BH','COR','BUS','BEM','BVL','ORF','PDBC','PRBC','PRT','PDT','PAG','CENTRAL')
 GROUP BY  T."IdTipoInstrumento",T."Nombre",C."IdMonedaInstrumento",C."TotalDatos"


