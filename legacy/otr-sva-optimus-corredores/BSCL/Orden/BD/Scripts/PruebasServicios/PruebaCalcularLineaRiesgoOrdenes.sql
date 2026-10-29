SET SCHEMA "BSCL";
drop TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
	"IdInterno"			BIGINT,
	"IdPortafolio"		BIGINT,
	"IdTipoOperacion"	NVARCHAR(6),
	"EsVentaCorta"		NVARCHAR(1),
	"EsODCuentaPropia"	NVARCHAR(1),
	"IdInstrumento" 	BIGINT, 
	"Monto" 			DECIMAL(26,8),
	"Tasa" 				DECIMAL(26,8),
	"PlazoEnDias" 		BIGINT,
	"IdBolsa" 			NVARCHAR(3),
	"IdTipoCanalIngreso" NVARCHAR(3)
);

--INSERT INTO #TT ("IdInterno","IdPortafolio","IdTipoOperacion","EsVentaCorta","EsODCuentaPropia","IdInstrumento","Monto","Tasa","PlazoEnDias","IdBolsa","IdTipoCanalIngreso")
--SELECT 1,343710,'VENACN','S','N',0,0,0,0,'BCS','AGE' FROM DUMMY;

--INSERT INTO #TT ("IdInterno","IdPortafolio","IdTipoOperacion","EsVentaCorta","EsODCuentaPropia","IdInstrumento","Monto","Tasa","PlazoEnDias","IdBolsa","IdTipoCanalIngreso")
--SELECT 2,343710,'COMFUT','N','N',330267,150000,0,0,'BCS','AGE' FROM DUMMY;


--INSERT INTO #TT ("IdInterno","IdPortafolio","IdTipoOperacion","EsVentaCorta","EsODCuentaPropia","IdInstrumento","Monto","Tasa","PlazoEnDias","IdBolsa","IdTipoCanalIngreso")
--SELECT 3,343710,'COMSIM','N','N',2249,150000,0,0,'BCS','AGE' FROM DUMMY;

--INSERT INTO #TT ("IdInterno","IdPortafolio","IdTipoOperacion","EsVentaCorta","EsODCuentaPropia","IdInstrumento","Monto","Tasa","PlazoEnDias","IdBolsa","IdTipoCanalIngreso")
--SELECT 4,343710,'VENSIM','N','N',2248,150000,0,0,'BCS','AGE' FROM DUMMY;

--INSERT INTO #TT ("IdInterno","IdPortafolio","IdTipoOperacion","EsVentaCorta","EsODCuentaPropia","IdInstrumento","Monto","Tasa","PlazoEnDias","IdBolsa","IdTipoCanalIngreso")
--SELECT 5,343710,'VENSIM','N','S',2248,150000,0,0,'BCS','AGE' FROM DUMMY;

INSERT INTO #TT ("IdInterno","IdPortafolio","IdTipoOperacion","EsVentaCorta","EsODCuentaPropia","IdInstrumento","Monto","Tasa","PlazoEnDias","IdBolsa","IdTipoCanalIngreso")
SELECT 1,343710,'VENSIM','N','S',2249,150000,0,0,'BCS','AGE' FROM DUMMY;


CALL "BSCL"."BSCL.Orden.BD.Servicios::CalcularLineaRiesgoOrdenes" (
	p_IdEmpresa						=> 1,
	p_IdUsuario						=> 4,
	p_IdModulo						=> 'ORD',
	p_tablaOperacion				=> #TT,
	p_out_Limites					=> ?,
	p_out_AdvertenciasContador		=> ?,
	p_out_AdvertenciasDetalle		=> ?,
	p_out_ErroresContador			=> ?,
	p_out_ErroresDetalle			=> ?
		
	);
	

