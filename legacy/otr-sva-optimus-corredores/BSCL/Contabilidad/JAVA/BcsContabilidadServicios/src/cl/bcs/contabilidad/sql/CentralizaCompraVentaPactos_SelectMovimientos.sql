WITH consultaCarteraPacto AS (
SELECT
pac."FolioCartera" as "FolioPacto",
pac."FechaOperacion" as "FechaPacto",
pac."IdEstado",
pac."IdMonedaSistema",
pac."IdTipoOperacion",
pac."Nemotecnico" as "NemotecnicoPacto",
pac."IdEmpresa",
Cli."DocumentoIdentidad",
pac."IdPortafolio"
FROM "BSCL.Custodia.BD::CUS.Cartera" as pac
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub on (pac."FolioCartera" = ultSub."FolioCarteraRelacionada")
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" as sby on (sby."IdSubyacente" = ultSub."IdSubyacente") 
	LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as Port ON (Port."IdPortafolio" = pac."IdPortafolio")
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as Cli ON (Cli."IdEmpresa" = pac."IdEmpresa" and Cli."IdCliente" = Port."IdCliente")
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipOpe ON (TipOpe."IdTipoOperacion" = pac."IdTipoOperacion")
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as Concepto ON (pac."IdConcepto" = Concepto."IdConcepto")
WHERE pac."IdTipoInstrumento" = 'PAC' and pac."IdEstado"IN ('VIG','VCD','ANT')
)
SELECT
  	TO_BIGINT(:IdTipoCentralizacion)							AS "IdTipoCentralizacion",
	ConsultaSubyacente."IdSubyacente"							AS "IdMovimiento",
	CASE CCP."NemotecnicoPacto"
		WHEN 'PACTO VENTA'	
			THEN	'PACTOVTAS'			
		WHEN 'PACTO COMPRA' 
			THEN  	'PACTOCOMPS'
		ELSE '????????????'			
	END															AS "IdTipoMovimiento",
	CCP."IdEmpresa"												AS "IdEmpresa",			
	CCP."FolioPacto" 											AS "Numero", 
	TO_BIGINT(0)												AS "Linea",
	YEAR(CCP."FechaPacto") ||'-'|| 
	RIGHT('0' || MONTH(CCP."FechaPacto"),2) ||'-'|| 
	RIGHT('0' || DAYOFMONTH(CCP."FechaPacto"),2)				AS "Fecha",
	CCP."DocumentoIdentidad" 									AS "DocumentoIdentidad",
	CCP."IdPortafolio"											AS "IdPortafolio",
	TO_BIGINT(YEAR(CCP."FechaPacto"))							AS "Ejercicio",
	RIGHT('0' || MONTH(CCP."FechaPacto"),2) ||'-'|| 
				 YEAR (CCP."FechaPacto") 						AS "Periodo",
	ConsultaSubyacente."MontoSubyacente"						AS "Monto",
	CCP."IdMonedaSistema"	 									AS "IdMoneda",
	CCP."IdEstado"												AS "Estado"     	
	FROM consultaCarteraPacto	AS CCP
	INNER  JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente"	ConsultaSubyacente
	ON (CCP."FolioPacto" = ConsultaSubyacente."FolioCarteraRelacionada")
	WHERE CCP."IdEstado" IN ('VIG','VCD','ANT') AND CCP."FechaPacto" = :FechaContable
UNION ALL
SELECT
    TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion",
    ConsultarCartera."IdCartera"       AS "IdMovimiento",
    CASE ConsultarCartera."IdTipoOperacion"
        WHEN 'VENPAC'   THEN 'OPACTOVTAS'            
        WHEN 'COMPAC'   THEN 'OPACTOCOMP'

        ELSE '????????????'         
    END                                                 AS "IdTipoMovimiento",
    ConsultarCartera."IdEmpresa"                        AS "IdEmpresa",                                     
    ConsultarCartera."FolioCartera"                     AS "Numero",
    TO_BIGINT(0)                                        AS "Linea",
    YEAR(ConsultarCartera."FechaOperacion") ||'-'|| RIGHT('0' || MONTH(ConsultarCartera."FechaOperacion"),2) ||'-'|| RIGHT('0' || DAYOFMONTH(ConsultarCartera."FechaOperacion"),2) AS "Fecha",
    ConsultarCartera."DocumentoIdentidad"               AS "DocumentoIdentidad",  
    ConsultarCartera."IdPortafolio"                     AS "IdPortafolio",
    TO_BIGINT(YEAR(ConsultarCartera."FechaOperacion"))       AS "Ejercicio",
    RIGHT('0' || MONTH(ConsultarCartera."FechaOperacion"),2) ||'-'|| YEAR(ConsultarCartera."FechaOperacion")  AS "Periodo",
    ABS(Operacion."MontoOperacionInicial")  			AS "Monto",     
    ConsultarCartera."IdMonedaSistema"                  AS "IdMoneda",                                
    ConsultarCartera."IdEstado"                         AS "Estado"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera"                    ConsultarCartera
INNER JOIN "BSCL.Mesa.BD::MES.Operacion" Operacion ON (Operacion."FolioCartera" = ConsultarCartera."FolioCartera")
WHERE ConsultarCartera."IdEstado" in ('VIG','VCD','ANT') AND ConsultarCartera."FechaOperacion" = :FechaContable;