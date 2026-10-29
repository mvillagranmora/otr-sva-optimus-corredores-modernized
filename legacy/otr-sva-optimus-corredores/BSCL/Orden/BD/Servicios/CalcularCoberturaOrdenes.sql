SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Orden.BD.Servicios::CalcularCoberturaOrdenes";
GO
CREATE PROCEDURE
--  Proposito: 
--  Autor: - 
--  Fecha Creacion: 

 "BSCL.Orden.BD.Servicios::CalcularCoberturaOrdenes" (
    IN p_IdEmpresa            BIGINT,
    IN p_IdUsuario            BIGINT,
    IN p_IdModulo             NVARCHAR(3),
	IN p_UsaReglasNegocio	  nvarchar(1),	-- S/N se usa en caso de servicios anidados.
	 
    IN p_IdPortafolio 			BIGINT,
	IN p_IdBolsa    			NVARCHAR(3),
	IN p_IdInstrumento			BIGINT,
	IN p_IdTipoInstrumento		NVARCHAR(3),
    IN p_EsVentaCorta			NVARCHAR(1),
	IN p_Simulacion   			TABLE("IdReferencial"     BIGINT, "IdInstrumento" BIGINT, "Cantidad" DECIMAL(26,8), "PrecioMercado" DECIMAL(26,8),"Monto" DECIMAL(26,8)),

    OUT p_out_TableCoberturaSimulacion TABLE("IdReferencial" BIGINT, "IdInstrumento" BIGINT,"Cantidad"  DECIMAL(26,8),"PrecioMercado" DECIMAL(26,8),"Monto" DECIMAL(26,8), "PorcentajeCoberturaEntidad" DECIMAL(26,8),"MontoCoberturaEntidad" DECIMAL(26,8),"PorcentajeCoberturaCorredora" DECIMAL(26,8),"MontoCoberturaCorredora" DECIMAL(26,8)),
	OUT p_out_TableCoberturaPortafolio TABLE("MontoCoberturaEntidad" DECIMAL(26,8), "MontoCoberturaCorredora" DECIMAL(26,8)),
        
	OUT p_out_AdvertenciasContador INT,
	OUT p_out_AdvertenciasDetalle  TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),
	OUT p_out_ErroresContador      INT,
	OUT p_out_ErroresDetalle       TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000)) 
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
BEGIN
	
	DECLARE v_IdInstrumento 					BIGINT;
	DECLARE v_IdTipoInstrumentoExigeGarantia 	NVARCHAR(3);
	
	DECLARE v_countEntidadDestinoGarantia INT;
	DECLARE v_IdEntidadDestinoGarantia 	BIGINT;
	
	IF (p_EsVentaCorta = 'S') THEN
		SELECT "IdInstrumentoPrestamo" INTO v_IdInstrumento from "BSCL.Custodia.BD.Vistas::PRT.ConsultarTipoPrestamo" where "IdTipoPrestamo" = 'PRTVTC';
		v_IdTipoInstrumentoExigeGarantia = 'PRT';
	ELSE
		SELECT p_IdInstrumento,p_IdTipoInstrumento into v_IdInstrumento,v_IdTipoInstrumentoExigeGarantia FROM DUMMY;
	END IF;

	SELECT COUNT(1) into v_countEntidadDestinoGarantia 
	FROM "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento"
	WHERE "IdTipoInstrumento" = :v_IdTipoInstrumentoExigeGarantia and "IdBolsa" = :p_IdBolsa;
	
	IF (:v_countEntidadDestinoGarantia > 0) THEN
		
		SELECT "IdEntidadDestinoGarantia" into v_IdEntidadDestinoGarantia 
		FROM "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento"
		WHERE "IdTipoInstrumento" = :v_IdTipoInstrumentoExigeGarantia and "IdBolsa" = :p_IdBolsa;
		
		CALL "BSCL.Plataforma.BD.Servicios::CalcularCobertura"(
	    	p_IdEmpresa						=> :p_IdEmpresa,
		    p_IdUsuario						=> :p_IdUsuario,
		    p_IdModulo						=> :p_IdModulo,
			p_UsaReglasNegocio				=> :p_UsaReglasNegocio,
			p_IdPortafolio					=> :p_IdPortafolio,
			p_IdEntidadDestinoGarantia		=> :v_IdEntidadDestinoGarantia,
			p_IdTipoInstrumentoExigeGarantia=> :v_IdTipoInstrumentoExigeGarantia,
		    p_IdInstrumentoExigeGarantia	=> :v_IdInstrumento,
			p_Simulacion					=> :p_Simulacion,
			
		    p_out_TableCoberturaSimulacion		=> p_out_TableCoberturaSimulacion,
			p_out_TableCoberturaPortafolio		=> p_out_TableCoberturaPortafolio,
		    p_out_AdvertenciasContador			=> p_out_AdvertenciasContador,
			p_out_AdvertenciasDetalle			=> p_out_AdvertenciasDetalle,
			p_out_ErroresContador				=> p_out_ErroresContador,
			p_out_ErroresDetalle				=> p_out_ErroresDetalle);
	END IF;
END;

GO