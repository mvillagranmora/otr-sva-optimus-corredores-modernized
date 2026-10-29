SET SCHEMA "BSCL";

CREATE LOCAL TEMPORARY TABLE #TT (
	"IdInterno"			BIGINT,
	"Nemotecnico"		NVARCHAR(40),
	"ISIN"				NVARCHAR(40),
	"CorteMinimo"		DECIMAL(26,8),
	"EstaInscrito"		NVARCHAR(1),
	"RazonSocial"		NVARCHAR(100),
	"FechaEmision"		NVARCHAR(10),
	"FechaVencimiento"	NVARCHAR(10),
	"IdEmisor"			NVARCHAR(40),
	"IdTipoReajuste"	NVARCHAR(3),
	"IdMoneda"			NVARCHAR(3),
	"IdTipoInstrumento"	NVARCHAR(3),
	"IdEstado"			NVARCHAR(3),
	"Tamano"			BIGINT,
	"IdEntidadClasificadoraRiesgo"	NVARCHAR(10),
	"ValorClasificacionRiesgo"		NVARCHAR(20)	
	
	);

INSERT INTO #TT("IdInterno","Nemotecnico","ISIN","CorteMinimo","EstaInscrito","RazonSocial",
	"FechaEmision","FechaVencimiento","IdEmisor","IdTipoReajuste","IdMoneda","IdTipoInstrumento",
	"IdEstado","Tamano","IdEntidadClasificadoraRiesgo","ValorClasificacionRiesgo")
	SELECT 0 as "IdInterno",'NADIA' AS "Nemotecnico",'' AS "ISIN",10 AS "CorteMinimo",'N' AS "EstaInscrito",'NADIA INC' AS "RazonSocial",
	NULL AS "FechaEmision",NULL AS "FechaVencimiento",'' AS "IdEmisor",'CLP' AS "IdTipoReajuste",'CLP' AS "IdMoneda",'BTU' AS "IdTipoInstrumento",
	'ACT' AS "IdEstado",1 AS "Tamano",'' AS "IdEntidadClasificadoraRiesgo",'' AS "ValorClasificacionRiesgo"
	FROM DUMMY;

CALL "BSCL.Plataforma.BD.Servicios::INS.CrearInstrumento" ( 
    p_IdEmpresa 		=> 1,
    p_IdUsuario 		=> 3,
    p_IdModulo       	=> 'ORD',
    p_simularOperacion	=> 'N',
	p_UsaReglasNegocio	=> 'N',

	p_Instrumentos => #TT,
    
    p_out_Resultado					=> ?,		
	p_out_AdvertenciasContador		=> ?,		
	p_out_AdvertenciasDetalle		=> ?,		
	p_out_ErroresContador 			=> ?,		
	p_out_ErroresDetalle			=> ?		
   );
