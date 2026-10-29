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
SELECT null,null,null,249184,153953
FROM DUMMY;

INSERT INTO #TT2
SELECT null,null,null,null,null,null,null,null
FROM DUMMY;

CALL "BSCL.Custodia.BD.Servicios::ConsultarFlujoCustodia_" ( 
			    p_IdEmpresa				=> 1,
				p_Uso					=> 'PARA_GARANTIZAR',
				p_IdPais				=> 44,
				p_Instrumentos			=> #TT2,
				p_Carteras				=> #TT,
				p_FechaSaldo			=> '2015-09-25',
			    p_out_FlujoDetallado	=> ?,
			    p_out_FlujoTotalizado	=> ?,
			    p_DiasProyeccion 		=> 0
			);