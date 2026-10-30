SET SCHEMA BSCL;
GO
DROP PROCEDURE "BSCL.CanalAgente.BD.Servicios::ConsultarHistorialMovimientosCuentaInversion";
GO
CREATE PROCEDURE
-- 
--  Proposito: Consulta Historial de Movimientos Cuenta Inversion 
--  Autor: rENZo Ulloa
--  Fecha Creacion: 
--   
"BSCL.CanalAgente.BD.Servicios::ConsultarHistorialMovimientosCuentaInversion" (
	IN p_IdEmpresa          BIGINT,
	IN p_IdUsuario          BIGINT,
	IN p_IdModulo           NVARCHAR(3),
	
	IN p_IdPortafolio		BIGINT,
	IN p_FechaDesde			NVARCHAR(10),
	IN p_FechaHasta			NVARCHAR(10),
	IN p_IdMoneda			NVARCHAR(3),
	IN p_IdMatrizAgrupador	BIGINT            DEFAULT NULL,
	IN p_EsADC			    NVARCHAR(1)       DEFAULT NULL,

	OUT p_SaldoEfectivoInicial		DECIMAL(26,8),	
	OUT p_SaldoEfectivoFinal		DECIMAL(26,8),	
	OUT p_out_Resultado				TABLE (
										"IdCargoAbonoMovimiento"	BIGINT,
										"NombreCliente"				NVARCHAR(302),
										"IdCliente"					BIGINT,
										"IdPortafolio"				BIGINT,
										"FechaMovimiento"			DATE,
										"FechaLiquidacion"			DATE,
										"IdMoneda"					NVARCHAR(3),	
										"IdConcepto"				NVARCHAR(6),
										"NombreConcepto"			NVARCHAR(100),
										"Cargo"						DECIMAL(26,8),
										"Abono"						DECIMAL(26,8),
										"FolioComprobante"			NVARCHAR(40),
										"IdEstado"					NVARCHAR(3),
										"NombreEstado"				NVARCHAR(100),
										"NombreTipoComprobante"		NVARCHAR(100),
										"NombreTipoProducto"		NVARCHAR(100),
										"NombreTipoOperacion"		NVARCHAR(100),
										"NombreTipoMovimiento"		NVARCHAR(100),
										"SaldoSimulado"				DECIMAL(26,8),
										"CodigoPortafolio"			NVARCHAR(100),
										"IdComprobante"				DECIMAL(26,8),
										"IdRespuestaDTE"			DECIMAL(26,8),
										"RespuestaPdfDTE"			NVARCHAR(100),
										"DocumentoIdentidad"		NVARCHAR(40),
										"IdMatrizAgrupador"         BIGINT,
										"EsADC"                     NVARCHAR(1)
										
									) DEFAULT EMPTY,
	
	
	OUT p_out_AdvertenciasContador 	INT,
	OUT p_out_AdvertenciasDetalle 	TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)) DEFAULT EMPTY,	
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(520)) DEFAULT EMPTY
	 
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	DECLARE v_out_InformacionContador1,v_out_AdvertenciasContador1,v_out_ErroresContador1 INT;
	DECLARE v_out_InformacionContador2,v_out_AdvertenciasContador2,v_out_ErroresContador2 INT;
	DECLARE v_IdCliente BIGINT;
	
	v_CuentaInversionAConsultar = SELECT null as "IdEjecutivo",:p_IdPortafolio AS "IdPortafolio",:p_IdMoneda AS "IdMoneda"
									FROM DUMMY;
									
	v_Cliente = SELECT "IdCliente" FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" where "IdPortafolio" = :p_IdPortafolio;
	IF (IS_EMPTY(:v_Cliente)) THEN
		RETURN;
	END IF;
	
	select "IdCliente" into v_IdCliente FROM :v_Cliente;
	
	/*CALL "BSCL.CuentaInversion.BD.Servicios::ConsultarSaldoCuentaInversion" ( 
 		p_IdEmpresa						=> :p_IdEmpresa,
    	p_IdUsuario						=> :p_IdUsuario,
    	p_IdModulo         				=> :p_IdModulo,
		p_UsaReglasNegocio				=> 'S',
		p_CuentaInversionAConsultar		=> :v_CuentaInversionAConsultar,
		p_FechaDesde  					=> :p_FechaDesde,
    	p_FechaHasta  					=> :p_FechaHasta,
    	
   		p_out_SaldoInicialFinal			=> v_out_SaldoInicialFinal,
   		p_out_SaldoPorFechaEfectiva 	=> v_out_SaldoPorFechaEfectiva,
		p_out_SaldoPorFechaMovimiento	=> v_out_SaldoPorFechaMovimiento,
	
		p_out_InformacionContador  		=> v_out_InformacionContador1,
		p_out_InformacionDetalle   		=> v_out_InformacionDetalle1,
		p_out_AdvertenciasContador      => v_out_AdvertenciasContador1,
		p_out_AdvertenciasDetalle       => v_out_AdvertenciasDetalle1,
		p_out_ErroresContador           => v_out_ErroresContador1,
		p_out_ErroresDetalle            => v_out_ErroresDetalle1);
	
	IF (NOT IS_EMPTY(:v_out_SaldoInicialFinal)) then
		SELECT "SaldoEfectivoInicial","SaldoEfectivoFinal" into	p_SaldoEfectivoInicial,p_SaldoEfectivoFinal
		FROM :v_out_SaldoInicialFinal;
	ELSE
		p_SaldoEfectivoInicial = 0;
		p_SaldoEfectivoFinal = 0;
	END IF;*/
	
	p_SaldoEfectivoInicial = 0;
	p_SaldoEfectivoFinal = 0;
	
	
	CALL "BSCL.CuentaInversion.BD.Servicios::ConsultarCartolaDeMovimiento" ( 
		p_IdEmpresa					=> :p_IdEmpresa,
    	p_IdUsuario                	=> :p_IdUsuario,
    	p_IdModulo                 	=> :p_IdModulo,
		p_UsaReglasNegocio			=> 'S',
	    p_IdCuentaInversion         => NULL,
		p_IdCliente                 => :v_IdCliente,
	    p_IdPortafolio              => :p_IdPortafolio,
	    p_IdMoneda                  => :p_IdMoneda,
	    p_IdEstado                  => NULL,
		p_FechaDesde 				=> :p_FechaDesde,
	    p_FechaHasta 				=> :p_FechaHasta,
	    p_SaldoPorFechaEfectiva		=> 'N',
	    p_IdMatrizAgrupador         => :p_IdMatrizAgrupador,
	    p_EsADC                     => :p_EsADC,
		
		p_out_CartolaDeMovimiento   => v_out_CartolaDeMovimiento,
		p_out_ErroresContador		=> v_out_ErroresContador2
	); 
	
	--SELECT * FROM :v_out_CartolaDeMovimiento;
	
	p_out_Resultado = SELECT car."IdCargoAbonoMovimiento",car."NombreCliente",car."IdCliente",car."IdPortafolio",car."FechaSistema" AS "FechaMovimiento",
							car."FechaLiquidacionOperacion" AS "FechaLiquidacion",car."IdMoneda",car."IdConcepto",car."NombreConcepto",car."Cargo",car."Abono",
							fac."FolioComprobante",car."IdEstado",car."NombreEstado",car."NombreTipoComprobante",car."NombreTipoProducto",car."NombreTipoOperacion",
							car."NombreTipoMovimiento",car."SaldoSimulado",por."CodigoPortafolio", car."IdComprobante", fac."IdRespuestaDTE",fac."RespuestaPdfDTE",
						    IDENT."DocumentoIdentidad",car."IdMatrizAgrupador",car."EsADC"
							FROM :v_out_CartolaDeMovimiento car
							LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" por ON por."IdPortafolio" = car."IdPortafolio"
							LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" cli ON cli."IdCliente" = por."IdCliente"
							LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = cli."IdPersona"
							LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
							
							LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS fac ON fac."IdComprobante" = car."IdComprobante"
							;


END
