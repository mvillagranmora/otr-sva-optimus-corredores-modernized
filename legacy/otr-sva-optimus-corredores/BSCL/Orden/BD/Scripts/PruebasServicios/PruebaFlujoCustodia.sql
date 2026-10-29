SET SCHEMA "BSCL";
drop TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
	"FolioCartera"						BIGINT, --TAMBIEN FOLIO CARTERA RELACIONADA EN OTRAS TABLAS
	"FolioGarantia"						BIGINT,
	"FolioSubyacente"					BIGINT,
	"IdCliente"							BIGINT,
	"IdPortafolio"						BIGINT
		
);

drop TABLE #TT2;
CREATE LOCAL TEMPORARY TABLE #TT2		(
	"IdClasificacionTipoInstrumento"	NVARCHAR(3),
	"IdTipoInstrumento"					NVARCHAR(3),
	"IdInstrumento"						BIGINT,
	"IdEmisor"							NVARCHAR(40),
	"FechaVencimiento"					DATE,
	"IdTipoReajuste"					NVARCHAR(3),
	"EsFungible"						NVARCHAR(1),
	"IdBoveda"							NVARCHAR(3)
		);
	
INSERT INTO #TT
SELECT null,null,null,null,null
FROM DUMMY;

INSERT INTO #TT2
SELECT null,null,122,null,null,null,null,null
FROM DUMMY;

CALL "BSCL.Custodia.BD.Servicios::ConsultarFlujoCustodia" ( 
			    p_IdEmpresa				=> 1,
				p_Uso					=> 'CUSTODIA',
				p_IdPais				=> 44,
				p_Instrumentos			=> #TT2,
				p_Carteras				=> #TT,
				p_FechaSaldo			=> '2016-12-05',
			    p_out_FlujoDetallado	=> ?,
			    p_out_FlujoTotalizado	=> ?,
			    p_DiasProyeccion 		=> 0,
			    p_EsAgrupadoPorFolioCartera => 'N'
			);
			
/*CALL "BSCL"."BSCL.Custodia.BD.Servicios::ConsultarCustodiaSubyacente" (
	-- Parametros entrada comunes en todos los servicios publicos
	1,
	1,
	'ORD',
	44,

	249184,
	153953,
	'SIM',
	'BCS',
	2	,?,?,?,?,?
			)*/
