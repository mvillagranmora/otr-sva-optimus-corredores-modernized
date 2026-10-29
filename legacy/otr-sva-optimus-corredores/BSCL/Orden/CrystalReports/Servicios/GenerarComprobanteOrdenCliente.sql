SET SCHEMA "BSCL"; 
GO 
DROP PROCEDURE "BSCL.Orden.CrystalReports.Servicios::GenerarComprobanteOrdenCliente"; 
GO
CREATE PROCEDURE
--
--  Proposito: Servicio para generar Comprobante de Orden
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 29/03/2016
--
--  MODIFICACION : 08-03-2018
--  Autor: CHERNANDEZ - BCS
--  Propisito: Agregar campos necesarios para comprobante de peru
--
"BSCL.Orden.CrystalReports.Servicios::GenerarComprobanteOrdenCliente" (
	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa				BIGINT,			--ID Empresa destino
	IN p_IdModulo				NVARCHAR(3),	--ID del Modulo que lo invoca
	-- Parametros entrada del servicio privado
	IN p_IdOrden				BIGINT
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	BEGIN
		--------------------
		--Variables
		--------------------
		DECLARE v_cantidad BIGINT;
		DECLARE v_monto BIGINT;
		DECLARE v_cantidad_texto NVARCHAR(1000);
		DECLARE v_monto_texto NVARCHAR(1000);
		DECLARE v_pais NVARCHAR(500);
		DECLARE v_codigoPais NVARCHAR(500);
		DECLARE p_out_Valor NVARCHAR(520);
		DECLARE v_logo TABLE("LogoCorredora" BLOB);
		DECLARE v_SegundosZonaHoraria BIGINT;
		DECLARE v_sesionLocale NVARCHAR(40);
		DECLARE v_nombre_moneda_orden NVARCHAR(50);
		select TOP 1 "NombrePais", "CodigoPais" INTO v_pais, v_codigoPais FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" CE
		INNER JOIN "BSCL.Plataforma.BD::REG.Pais" P ON CE."IdPais" = P."IdPais"
		WHERE CE."IdEmpresa" = p_IdEmpresa;
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion"(:p_IdEmpresa, 'PLA.SegundosZonaHoraria', :v_SegundosZonaHoraria);
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion"(:p_IdEmpresa, 'PLA.LocalizacionSistema', :v_sesionLocale);
		---------- Consulta Clientes ---------				  
		v_Clientes = select TOP 1
					CC."DocumentoIdentidad",
					CASE WHEN COALESCE(CO."RazonSocialPortafolio",'') = '' THEN CC."NombreCompleto" ELSE CO."RazonSocialPortafolio" END AS "Nombre/RSocial",
					UPPER(COALESCE(CPDF."NombreLargo",'')) AS "Direccion",
					COALESCE(CE."NombreCompleto",'') AS "Agente",
					CASE COALESCE(CE2."NombreCompleto",'') WHEN '' 
					THEN COALESCE(CO."NombreLargoCanal",'')
					ELSE COALESCE(CE2."NombreCompleto",'') END AS "Atendido",
					UPPER(COALESCE(CO."NombreProcedenciaOrden",'')) AS "Procedencia",--verificar funcionamiento de esta shit
					CO."CodigoPortafolio" As "Cuenta",
					TO_VARCHAR(CO."IdOrden") AS "NSecuencia",
					CO."FolioOrden",
					TO_NVARCHAR(ADD_SECONDS(CO."FechaOperacionOrden",:v_SegundosZonaHoraria), 'DD-MM-YYYY HH24:MI:SS') as "FechaOperacionOrden",
					CO."CantidadOrden",
					CO."InstrumentoUI",
					--"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(CO."PrecioOrden",CASE WHEN CO."IdMonedaOrden"='CLP' THEN 'i' ELSE 'd' END, CASE WHEN CO."IdMonedaOrden"='CLP' THEN 0 ELSE 3 END, v_codigoPais) AS "PrecioOrden",
					"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(CASE WHEN CO."TipoPrecio" = 'MERCADO' THEN 0 WHEN CO."IdTipoInstrumento" = 'SIM' AND TRA."IdTransaccion" IS NOT NULL THEN TRA."Precio" ELSE CO."PrecioOrden" END,'d', 3 , :v_codigoPais) AS "PrecioOrden",
					--CO."PrecioOrden" AS "PrecioOrdenNumeric",
					CASE WHEN CO."IdTipoInstrumento" = 'SIM' AND TRA."IdTransaccion" IS NOT NULL THEN TRA."Precio" ELSE CO."PrecioOrden" END AS "PrecioOrdenNumeric",
					--"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(CO."MontoOrden",CASE WHEN CO."IdMonedaOrden"='CLP' THEN 'i' ELSE 'd' END, CASE WHEN CO."IdMonedaOrden"='CLP' THEN 0 ELSE 3 END, v_codigoPais)  AS "MontoOrden",
					"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(CASE WHEN CO."IdTipoInstrumento" = 'SIM' AND TRA."IdTransaccion" IS NOT NULL THEN TRA."Monto" ELSE CO."MontoOrden" END,CASE WHEN CO."IdMonedaOrden"='CLP' THEN 'i' ELSE 'd' END, CASE WHEN CO."IdMonedaOrden"='CLP' THEN 0 ELSE 3 END, v_codigoPais)  AS "MontoOrden",
					--CO."MontoOrden" AS "MontoOrdenNumeric",
					CASE WHEN CO."IdTipoInstrumento" = 'SIM' AND TRA."IdTransaccion" IS NOT NULL THEN TRA."Monto" ELSE CO."MontoOrden" END AS "MontoOrdenNumeric",
					UPPER(COALESCE(MEDIO."Nombre",'')) AS "FormaPago",
					CASE WHEN TO_VARCHAR(CO."FechaOperacionOrden", 'YYYY-MM-DD') = CO."FechaVigencia" THEN 'SI' ELSE 'NO' END AS "OrdenParaDia",
					UPPER(CO."NombreCondicionDeLiquidacion") AS "NombreCondicionDeLiquidacion",
					CO."IdBolsa",
					v_pais AS "PaisOrigen",
					TO_VARCHAR(CO."FechaOperacionOrden", 'DD-MM-YYYY') AS "FechaVigencia",
					TO_VARCHAR(CO."FechaVigencia", 'DD-MM-YYYY') AS "FechaLiquidacion",
					CASE WHEN TO_VARCHAR(CO."FechaVencimiento", 'DD-MM-YYYY') = '01-01-0001' THEN '' ELSE TO_VARCHAR(CO."FechaVencimiento", 'DD-MM-YYYY') END AS "FechaVencimiento",
					CO."TasaVentaCorta",
					UPPER(COALESCE(CO."NombreClaseOrden",'')) AS "OrdenesRecibir",
					CO."IdMonedaOrden",
					CO."IdTipoInstrumento",
					CASE 
					   WHEN v_codigoPais = 'PE' THEN CO."AccionOrden" || ' ' || '' || PC."ProductoEspanol"
					   ELSE CO."NombreTipoOperacion"
					END AS "NombreTipoOperacion",
					CO."NombreAutorizadorOrden",
					CASE WHEN CO."EsVentaCorta" = 'S' THEN 'SI' ELSE 'NO' END AS "EsVentaCorta",
					TO_VARCHAR(COALESCE("PlazoSimultanea" ,0)) AS "PlazoSimultanea",
					"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(COALESCE("TasaSimultanea" ,0),'d',0) || '%' AS "TasaSimultanea",
					CASE WHEN CO."IdTipoInstrumento" IN ('FDO','FIP','FBC','FIC','FUF','IOA','IOE','IOI','IOM','IOB','IOC','IOU','IOL','IOD') THEN 'SI' ELSE 'NO' END AS "TipoInstrumentoDV",
					CO."Tamano" AS "LotePadron",
					CO."PrecioStrike",
					CO."EsDivisible" AS "ModalidadLiquidacion",
					--CO."TipoOpcion", --falta en desa pe
					'' AS "TipoOpcion",
					CO."EstiloOpcion",
					--SOLO PARA PERU
					CC."DocumentoIdentidadAdicional",
                    CC."TipoDocumentoIdentidadAdicional",
					DOC."IdTipoDocumentoIdentidadAsociado",
					DOC."DocumentoIdentidadAsociado",
					CO."IdCondicionDeLiquidacion",
                    CASE WHEN CO."EsOrdenEspecial" = 'S' THEN 'Especial' ELSE 'Ordinaria' END AS "EsOrdenEspecial",
                    CASE WHEN IFNULL(CO."FolioOrden",'') != '' THEN 
                        CASE SUBSTRING(CO."FolioOrden",0,1) 
                            WHEN '1' THEN 'ORDEN ORDINARIA'
                            WHEN '2' THEN 'ORDEN ADM - MODELO 1'
                            WHEN '3' THEN 'ORDEN ADM - MODELO 2'
                            WHEN '4' THEN 'ORDEN MILA'
                            WHEN '5' THEN 'ORDEN ESPECIAL'
                            WHEN '6' THEN 'FORMADOR DE MERCADO'
                            ELSE ''
                        END
                        ELSE ''
                    END AS "Serie",
                    CASE WHEN CO."IdClasificacionTipoInstrumento" = 'RE' THEN TO_VARCHAR(COALESCE("PlazoReporte" ,0)) ELSE '-' END AS "PlazoPeru",
                    CASE WHEN CO."IdClasificacionTipoInstrumento" = 'RF' THEN TO_VARCHAR(COALESCE("TasaRFIF" ,0)) 
                         WHEN CO."IdClasificacionTipoInstrumento" = 'RE' THEN TO_VARCHAR(COALESCE("TasaReporte" ,0))
                         ELSE '-'
                    END AS "TasaPeru",
                    CASE "IdTipoVigenciaOrden" 
                         WHEN 'SH' THEN '1 (dia)'
                         WHEN 'SE' THEN '5 (dias)'
                         WHEN 'OP' THEN '30 (dias)'
                         WHEN 'FF' THEN TO_VARCHAR(DAYS_BETWEEN(TO_DATE ("FechaOperacionOrden", 'YYYY-MM-DD HH24:MI:SS'), TO_DATE("FechaVigencia", 'YYYY-MM-DD'))) || ' (dias)'
                         ELSE ''
                     END AS "VigenciaOrden",
                    CO."PorcentajeCobros",
                    CO."DescripcionPropositoPortafolio" AS "DescripcionPortafolio",
                    --CO."Tasa",
                    CASE WHEN CO."IdTipoInstrumento" = 'SIM' AND TRA."IdTransaccion" IS NOT NULL THEN TRA."Tasa" ELSE CO."Tasa" END AS "Tasa",
                    CASE WHEN CO."IdTipoInstrumento" = 'SIM' AND TRA."IdTransaccion" IS NOT NULL THEN ASGREF."MontoAsignado" ELSE CO."MontoOrdenPlazo" END AS "MontoOrdenPlazo",
                    CASE WHEN CO."IdTipoInstrumento" = 'SIM' AND TRA."IdTransaccion" IS NOT NULL THEN 'SI' ELSE 'NO' END AS "EsAnexo19",
   					MONFOR."PrecioDecimales",
    				MONFOR."TasaDecimales",
					CO."InstrumentoNegociacion",
					CO."NombreBoveda",
					MON."Nombre" AS "NombreMoneda"
				FROM "BSCL.Orden.BD.Vistas::ConsultarOrden" AS CO
				INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CC ON CO."IdCliente" = CC."IdCliente"
				INNER JOIN  "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON CC."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad"
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" CPDF ON CPDF."IdDireccionFisica" = CC."IdDireccionFisicaPrincipal"
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE ON CE."IdEmpleado" = CO."IdEmpleadoPortafolio"
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE2 ON CE2."IdEmpleado" = CO."IdEmpleadoDigitador"					
			    LEFT JOIN  "BSCL.Facturacion.BD::FAC.ProductoCCB" PC ON CO."IdClasificacionTipoInstrumento" = PC."IdClasificacionTipoInstrumento" 
			     AND CO."IdTipoProducto" = PC."IdTipoProducto" AND CO."IdBolsa" = PC."IdBolsa" 
			     AND (CASE WHEN CO."IdTipoProducto" = 'INRF' THEN CASE WHEN CO."IdSistemaBursatil" = PC."IdSistemaBursatil" THEN 1 ELSE 0 END ELSE 1 END) = 1
				LEFT JOIN "BSCL.Orden.BD::ORD.FormaDePagoOrden" PAGO on PAGO."IdOrden" = CO."IdOrden"
				LEFT JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" MEDIO on MEDIO."IdMedioDePago" = PAGO."IdMedioDePago"
				LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" ASG ON ASG."IdOrden" = CO."IdOrden" AND ASG."EsAPlazo" ='N'
				LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" ASGREF ON ASG."IdAsignacionRef" = ASGREF."IdAsignacion"
				LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" TRA ON ASG."IdTransaccion" = TRA."IdTransaccion"
				LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" AS MONFOR ON (MONFOR."IdTipoInstrumento" = CO."IdTipoInstrumento" AND MONFOR."IdMoneda" = CO."IdMonedaOrden" AND MONFOR."IdTipoOperacion" = '')
				LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON ON MON."IdMoneda" = CO."IdMonedaOrden"
				WHERE MONFOR."IdUsoMonedaFormato" = 'GEN' AND CC."IdEmpresa" = :p_IdEmpresa AND CO."IdOrden" = :p_IdOrden;
		SELECT "MontoOrdenNumeric" INTO v_monto FROM :v_Clientes;
		SELECT "CantidadOrden" INTO v_cantidad FROM :v_Clientes;
	    SELECT "NombreMoneda" INTO v_nombre_moneda_orden FROM :v_Clientes;
		CALL "BSCL.Plataforma.BD.Servicios::ConvertirMontoEscrito"(v_monto,'CL','CLP', :v_monto_texto);
		CALL "BSCL.Plataforma.BD.Servicios::ConvertirMontoEscrito"(v_cantidad,'CL','CLP', :v_cantidad_texto);
		v_monto_t = select ('(' || v_monto_texto || ' ' || v_nombre_moneda_orden ||')') AS "MontoTexto" FROM DUMMY;
		v_cantidad_t = select ('(' || v_cantidad_texto || ')') AS "CantidadTexto" FROM DUMMY;
		v_comision= SELECT 
					COALESCE(SUM(COBO."MontoCobroCalculadoLocal"),0) AS "Comision", 
					(SELECT top 1 COALESCE(SUM("MontoCobroCalculadoLocal"),0) FROM "BSCL.Orden.BD::ORD.CobroOrden" WHERE "IdOrden" = p_IdOrden AND ("IdCobro" IN ('IVA','GAFI','GFFA','GFOD'))) AS IvaGastoFijo
					FROM "BSCL.Orden.BD.Vistas::ConsultarOrden" AS CO
					INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CC ON CO."IdCliente" = CC."IdCliente"
					INNER JOIN "BSCL.Orden.BD::ORD.CobroOrden" COBO ON CO."IdOrden" = COBO."IdOrden" AND (COBO."IdCobro" = 'COVA' OR COBO."IdCobro" = 'COFI' OR COBO."IdCobro" = 'COAI')
					WHERE CC."IdEmpresa" = p_IdEmpresa AND CO."IdOrden" = p_IdOrden;
		--COBROS PERU
		v_cobroSAB	=	select 
						COALESCE(SUM(COALESCE(COBO."MontoCobroCalculadoLiquidacion",0)),0) AS "ComisionSAB"
						FROM "BSCL.Orden.BD.Vistas::ConsultarOrden" AS CO
						INNER JOIN "BSCL.Orden.BD::ORD.CobroOrden" COBO ON CO."IdOrden" = COBO."IdOrden"
						INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro" def ON def."CodigoDefinicionCobro" = COBO."IdCobro"
						INNER JOIN "BSCL.Plataforma.BD::COM_2.ClaseCobro" cla ON cla."IdClaseCobro" = def."IdClaseCobro"
						WHERE CO."IdOrden" = p_IdOrden AND cla."IdClaseCobro" = 'COMISION_SAB';
		v_cobroBVL	=	select 
						COALESCE(SUM(COALESCE(COBO."MontoCobroCalculadoLiquidacion",0)),0) AS "ComisionBVL"
						FROM "BSCL.Orden.BD.Vistas::ConsultarOrden" AS CO
						INNER JOIN "BSCL.Orden.BD::ORD.CobroOrden" COBO ON CO."IdOrden" = COBO."IdOrden"
						INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro" def ON def."CodigoDefinicionCobro" = COBO."IdCobro"
						INNER JOIN "BSCL.Plataforma.BD::COM_2.ClaseCobro" cla ON cla."IdClaseCobro" = def."IdClaseCobro"
						WHERE CO."IdOrden" = p_IdOrden AND cla."IdClaseCobro" = 'COMISION_BVL';
		v_cobroFondoLiq	=	select 
						COALESCE(SUM(COALESCE(COBO."MontoCobroCalculadoLiquidacion",0)),0) AS "FondoLiquidacion"
						FROM "BSCL.Orden.BD.Vistas::ConsultarOrden" AS CO
						INNER JOIN "BSCL.Orden.BD::ORD.CobroOrden" COBO ON CO."IdOrden" = COBO."IdOrden"
						INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro" def ON def."CodigoDefinicionCobro" = COBO."IdCobro"
						INNER JOIN "BSCL.Plataforma.BD::COM_2.ClaseCobro" cla ON cla."IdClaseCobro" = def."IdClaseCobro"
						WHERE CO."IdOrden" = p_IdOrden AND cla."IdClaseCobro" = 'FONDO_LIQUIDACION';
		v_cobroFondoGar	=	select 
						COALESCE(SUM(COALESCE(COBO."MontoCobroCalculadoLiquidacion",0)),0) AS "FondoGarantia"
						FROM "BSCL.Orden.BD.Vistas::ConsultarOrden" AS CO
						INNER JOIN "BSCL.Orden.BD::ORD.CobroOrden" COBO ON CO."IdOrden" = COBO."IdOrden"
						INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro" def ON def."CodigoDefinicionCobro" = COBO."IdCobro"
						INNER JOIN "BSCL.Plataforma.BD::COM_2.ClaseCobro" cla ON cla."IdClaseCobro" = def."IdClaseCobro"
						WHERE CO."IdOrden" = p_IdOrden AND cla."IdClaseCobro" = 'FONDO_GARANTIA';
		v_cobroSMV	=	select 
						COALESCE(SUM(COALESCE(COBO."MontoCobroCalculadoLiquidacion",0)),0) AS "ComisionSMV"
						FROM "BSCL.Orden.BD.Vistas::ConsultarOrden" AS CO
						INNER JOIN "BSCL.Orden.BD::ORD.CobroOrden" COBO ON CO."IdOrden" = COBO."IdOrden"
						INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro" def ON def."CodigoDefinicionCobro" = COBO."IdCobro"
						INNER JOIN "BSCL.Plataforma.BD::COM_2.ClaseCobro" cla ON cla."IdClaseCobro" = def."IdClaseCobro"
						WHERE CO."IdOrden" = p_IdOrden AND cla."IdClaseCobro" = 'COMISION_SMV';
		v_cobroCAVALI	=	select 
						COALESCE(SUM(COALESCE(COBO."MontoCobroCalculadoLiquidacion",0)),0) AS "ComisionCAVALI"
						FROM "BSCL.Orden.BD.Vistas::ConsultarOrden" AS CO
						INNER JOIN "BSCL.Orden.BD::ORD.CobroOrden" COBO ON CO."IdOrden" = COBO."IdOrden"
						INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro" def ON def."CodigoDefinicionCobro" = COBO."IdCobro"
						INNER JOIN "BSCL.Plataforma.BD::COM_2.ClaseCobro" cla ON cla."IdClaseCobro" = def."IdClaseCobro"
						WHERE CO."IdOrden" = p_IdOrden AND cla."IdClaseCobro" = 'COMISION_CAVALI';
		v_cobroIGV	=	select 
						COALESCE(SUM(COALESCE(COBO."MontoCobroCalculadoLiquidacion",0)),0) AS "ImpuestoIGV"
						FROM "BSCL.Orden.BD.Vistas::ConsultarOrden" AS CO
						INNER JOIN "BSCL.Orden.BD::ORD.CobroOrden" COBO ON CO."IdOrden" = COBO."IdOrden"
						INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro" def ON def."CodigoDefinicionCobro" = COBO."IdCobro"
						INNER JOIN "BSCL.Plataforma.BD::COM_2.ClaseCobro" cla ON cla."IdClaseCobro" = def."IdClaseCobro"
						WHERE CO."IdOrden" = p_IdOrden AND cla."IdClaseCobro" = 'IMPUESTO_IGV';
		v_cobroGenerico	=	select 
						COALESCE(SUM(COALESCE(COBO."MontoCobroCalculadoLiquidacion",0)),0) AS "OtrasComisiones"
						FROM "BSCL.Orden.BD.Vistas::ConsultarOrden" AS CO
						INNER JOIN "BSCL.Orden.BD::ORD.CobroOrden" COBO ON CO."IdOrden" = COBO."IdOrden"
						INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro" def ON def."CodigoDefinicionCobro" = COBO."IdCobro"
						INNER JOIN "BSCL.Plataforma.BD::COM_2.ClaseCobro" cla ON cla."IdClaseCobro" = def."IdClaseCobro"
						WHERE CO."IdOrden" = p_IdOrden AND cla."IdClaseCobro" = 'COMISION_GENERICA';
		v_datosGenerales = select 
		"RazonSocial" AS "RazonCorredora",
		("DireccionFisicaPrincipalCalle" || '' || "DireccionFisicaPrincipalNumero" || ' ' ||"DireccionFisicaPrincipalDepartamento") AS "DireccionCorredora",
        ("DireccionFisicaPrincipalNombreDivisionPais" || ', ' || "DireccionFisicaPrincipalNombreCiudad" || ', ' || "DireccionFisicaPrincipalNombrePais") AS "DivisionCorredora",
		"DocumentoIdentidad" AS "RutCorredora", 
		"TelefonoPrincipal" AS "TelefonoCorredora" 
		from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa"
		WHERE "IdEmpresa" = p_IdEmpresa;
		v_logo = select TOP 1
		"Binario" AS "LogoCorredora"
		from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' AND "IdEmpresa" = p_IdEmpresa ORDER BY "Version" DESC;
		--------------------
		--Logica de Negocio 
		--------------------
		BEGIN
			IF (v_sesionLocale = 'PE') THEN
				SELECT * FROM :v_Clientes 
				CROSS JOIN :v_cobroSAB
				CROSS JOIN :v_cobroBVL
				CROSS JOIN :v_cobroFondoLiq
				CROSS JOIN :v_cobroFondoGar
				CROSS JOIN :v_cobroSMV
				CROSS JOIN :v_cobroCAVALI
				CROSS JOIN :v_cobroIGV
				CROSS JOIN :v_cobroGenerico
				CROSS JOIN :v_monto_t
				CROSS JOIN :v_cantidad_t
				CROSS JOIN :v_datosGenerales
				CROSS JOIN :v_logo;
			ELSE
				SELECT * FROM :v_Clientes 
				CROSS JOIN :v_comision
				CROSS JOIN :v_monto_t
				CROSS JOIN :v_cantidad_t
				CROSS JOIN :v_datosGenerales
				CROSS JOIN :v_logo;
			END IF;
		END;
		--------------------
		--FIN SP 
		--------------------
	END;
END;