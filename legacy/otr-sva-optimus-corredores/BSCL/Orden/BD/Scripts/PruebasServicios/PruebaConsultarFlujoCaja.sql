SET SCHEMA "BSCL";
drop TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
	"IdPortafolio"	BIGINT,		  -- Parámetro opcional
    "IdMoneda"		NVARCHAR(3) -- Parámetro opcional
);

INSERT INTO #TT ("IdPortafolio","IdMoneda")
SELECT 20197,'CLP' FROM DUMMY;


CALL "BSCL.Plataforma.BD.Servicios::ConsultarFlujoCaja" ( 
	-- Parametros entrada comunes en todos los servicios publicos
    p_IdEmpresa                     => 1,
    p_IdUsuario                     => 3,
    p_IdModulo                      => 'ORD',
    p_UsaReglasNegocio				=> 'S',
    p_Uso							=> 'ORDENES_DE_COMPRA',
    p_FechaDesde  					=> '2016-08-23',
    p_FechaHasta					=> '2016-08-26',
    p_PortafolioAConsultar			=> #TT,
    p_out_FlujoCaja 				=> ?,
    P_OUT_FLUJOCAJADETALLADO 		=> ?,
	p_out_INFORMACIONCONTADOR	=> ?,
	p_out_INFORMACIONDetalle	=> ?,
	p_out_AdvertenciasContador		=> ?,
	p_out_AdvertenciasDetalle		=> ?,
	p_out_ErroresContador			=> ?,
	p_out_ErroresDetalle 			=> ?
	
) 
