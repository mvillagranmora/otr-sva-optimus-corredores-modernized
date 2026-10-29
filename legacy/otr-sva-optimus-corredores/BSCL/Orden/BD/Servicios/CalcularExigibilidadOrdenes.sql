SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Orden.BD.Servicios::CalcularExigibilidadOrdenes";
GO
CREATE PROCEDURE
--  Proposito: 
--  Autor: - 
--  Fecha Creacion: 

 "BSCL.Orden.BD.Servicios::CalcularExigibilidadOrdenes" (
    --VARIABLES OBLIGATORIAS
    IN p_IdEmpresa          	          	BIGINT,      -- identificador de la empresa de la orden 
    IN p_IdUsuario	                        BIGINT,      -- usuario que ingresa la orden 
    IN p_IdModulo                           NVARCHAR(3), -- modulo de la orden 
    IN p_simularOperacion					NVARCHAR(1), -- S/N se usa para simular operacion (algunos casos)
	IN p_UsaReglasNegocio	 				NVARCHAR(1), -- S/N se usa en caso de servicios anidados.
    
    IN p_TableExigibilidadSimulacion    	TABLE(
    											"IdInterno"			BIGINT,
    											"IdInstrumento" 	BIGINT,				--ID SUBYACENTE, FUTUROS???
    											"Cantidad" 			DECIMAL(26,8),
    											"Precio" 			DECIMAL(26,8),
    											"Monto" 			DECIMAL(26,8),
    											"IdBolsa" 			NVARCHAR(3)
    											),
  	IN p_EsVentaCorta						NVARCHAR(1),
    IN p_IdTipoInstrumento 					NVARCHAR(3),		--SIM, FDO
    IN p_IdPortafolio 						BIGINT, 
    IN p_IdBolsa 							NVARCHAR(3),
    IN p_IdInstrumento                      BIGINT,				--ID INSTRUMENTO QUE EXIGE
    
	OUT p_out_TableExigibilidadSimulacion   TABLE("IdTipoInstrumento" NVARCHAR(3),"IdInstrumento" BIGINT,"Cantidad" DECIMAL(26,8),"Precio" DECIMAL(26,8),"Monto" DECIMAL(26,8),"IdBolsa" NVARCHAR(3),"MontoExigibilidadEntidad" Decimal(26,8),"MontoExigibilidadCorredora" Decimal(26,8),"PorcentajeExigibilidadEntidad" Decimal(26,8),"PorcentajeExigibilidadCorredora" Decimal(26,8)),
	OUT p_out_TableExigibilidadPortafolio   TABLE("MontoExigibilidadEntidad" DECIMAL(26,8),"MontoExigibilidadCorredora" DECIMAL(26,8)), 
	OUT p_out_AdvertenciasContador			INT,
	OUT p_out_AdvertenciasDetalle			TABLE ("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
	OUT p_out_ErroresContador 				INT,
	OUT p_out_ErroresDetalle 				TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
 
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
BEGIN
	
	DECLARE v_IdTipoInstrumento NVARCHAR(3);
	DECLARE v_IdInstrumento 	BIGINT;
	
	IF (p_EsVentaCorta = 'S') THEN
		SELECT 'PRT' INTO v_IdTipoInstrumento FROM DUMMY;
		SELECT "IdInstrumentoPrestamo" INTO v_IdInstrumento from "BSCL.Custodia.BD.Vistas::PRT.ConsultarTipoPrestamo" where "IdTipoPrestamo" = 'PRTVTC';
	ELSE
		v_IdTipoInstrumento = :p_IdTipoInstrumento;
		v_IdInstrumento = :p_IdInstrumento;
	END IF;
	
	v_TableExigibilidadSimulacion = SELECT "IdInterno",:v_IdTipoInstrumento as "IdTipoInstrumento","IdInstrumento","Cantidad","Precio","Monto","IdBolsa" 
										FROM :p_TableExigibilidadSimulacion pla;
	
	CALL "BSCL.Plataforma.BD.Servicios::CalcularExigibilidad" (
	    p_IdEmpresa							=> :p_IdEmpresa,
	    p_IdUsuario							=> :p_IdUsuario,
	    p_IdModulo							=> :p_IdModulo,
	    p_simularOperacion					=> 'N',
		p_UsaReglasNegocio					=> 'N',
	    p_TableExigibilidadSimulacion		=> :v_TableExigibilidadSimulacion,
	    p_IdTipoInstrumento					=> :v_IdTipoInstrumento,
	    p_IdPortafolio						=> :p_IdPortafolio,
	    p_IdBolsa							=> :p_IdBolsa,
	    p_IdInstrumento						=> :v_IdInstrumento,
	    
		p_out_TableExigibilidadSimulacion	=> p_out_TableExigibilidadSimulacion,
		p_out_TableExigibilidadPortafolio	=> p_out_TableExigibilidadPortafolio,
		p_out_AdvertenciasContador			=> p_out_AdvertenciasContador,
		p_out_AdvertenciasDetalle			=> p_out_AdvertenciasDetalle,
		p_out_ErroresContador				=> p_out_ErroresContador,
		p_out_ErroresDetalle				=> p_out_ErroresDetalle);


END;

