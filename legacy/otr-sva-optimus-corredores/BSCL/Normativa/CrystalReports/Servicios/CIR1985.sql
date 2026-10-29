SET SCHEMA "BSCL"; 
GO 
DROP PROCEDURE "BSCL.Normativa.CrystalReports.Servicios::CIR1985"; 
GO

 
CREATE PROCEDURE

"BSCL.Normativa.CrystalReports.Servicios::CIR1985" (
	-- Parametros entrada comunes en todos los servicios privados
  	IN p_IdEmpresa			BIGINT,

    --VARIABLES DEL PROCESO
	IN	p_IdCliente 							BIGINT 			DEFAULT '0',
	IN  p_IdPortafolio                          BIGINT 			DEFAULT '0',
	IN  p_FechaIngresoSolicitud		            NVARCHAR(10)    DEFAULT '' --'YYYY-MM-DD'
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN

	
		--------------------
		--Variables
		--------------------
		DECLARE v_Count	 BIGINT:=0;
		DECLARE v_MensajeError		NVARCHAR(100) := '';

	
		v_logo = SELECT TOP 1 "Binario" AS "LogoCorredora"
		         FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		         WHERE "NombreImagen" = 'LOGO_CORREDOR' 
		         ORDER BY "Version" DESC;
		         
		--- Datos de Cliente
		v_DatosCliente = SELECT CASE 
                 WHEN LENGTH(Per."Nombres") > 0 THEN Per."Nombres" || ' ' || Per."ApellidoPaterno" || ' ' || Per."ApellidoMaterno"
               ELSE Per."RazonSocial" 
               END AS "NombreClienteCompleto",
               DocIden."DocumentoIdentidad",
               Por."CodigoPortafolio",
               (IfNull(DirFisica."Calle", '') || ' ' || IfNull(DirFisica."Numero", '') || ' ' || IfNull(DirFisica."Departamento",'')) || ',  ' || CASE
                                                                                                                                                    WHEN LENGTH(DivPais."NombreDivisionPais") > 0 THEN DivPais."NombreDivisionPais"
                                                                                                                                                  ELSE 'En definicion'
                                                                                                                                                  END
               AS "DireccionFisica", 
               CASE 
                 WHEN LENGTH(Ciudad."NombreCiudad") > 0 THEN Ciudad."NombreCiudad"
               ELSE 'En definicion'
               END AS "Ciudad",
		       Tel."Numero" AS "Telefono",
		       DirElec."Direccion" AS "Email",
		       TO_VARCHAR(:p_FechaIngresoSolicitud, 'DD/MM/YYYY') AS "FechaIngresoSolicitud",
		       Orden."Nemotecnico",
		       Orden."IdCondicionDeLiquidacion",
		       Orden."Folio",
		       Orden."Cantidad",
		       Orden."PrecioOrden",
		       Orden."MontoOrden",
		       TO_VARCHAR(Orden."FechaRecepcion", 'DD/MM/YYYY HH:MI:SS') AS "FechaRecepcion",
		       TO_VARCHAR(Orden."FechaAceptacion", 'DD/MM/YYYY HH:MI:SS') AS "FechaAceptacion",
		       Orden."CodigoClaseOrden",
		       IFNULL(MvtoCmbte."FolioTransaccion", '0') AS "FolioTransaccion",
		       IFNULL(MvtoCmbte."Cantidad", 0) AS "CantidadFacturacion",
		       IFNULL(MvtoCmbte."Precio", 0) AS "Precio",
		       IFNULL(MvtoCmbte."Monto", 0) AS "Monto",
		       IFNULL(TO_VARCHAR(MvtoCmbte."FechaMovimientoComprobante", 'DD/MM/YYYY HH:MI:SS'), '') AS "FechaMovimientoComprobante",
		       IFNULL(MvtoCmbte."IdBolsa", '') AS "IdBolsa",
		       IFNULL(MvtoCmbte."IdCorredoraContraparte", 0) AS "IdCorredoraContraparte",
		       ConsEje."NombreCompleto" AS "NombreAgente",
		       CASE 
		         WHEN ConsMvtoCmbteCobroTot."COMMVTO" <> NULL THEN ConsMvtoCmbteCobroTot."COMMVTO"
		         WHEN ConsMvtoCmbteCobroTot."COMMVTO" > 0 THEN ConsMvtoCmbteCobroTot."COMMVTO"
		       ELSE 0
		       END AS "Comision",
		       Empresa."Nombre" AS "NombreEmpresa" ,
		       Logo."LogoCorredora" AS "LogoCorredora",
			   "OrdenesUI"."InstrumentoNegociacion",
			   Orden."IdMonedaLiquidacionOrden" AS "IdMoneda"
		FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cli
		  INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS Per
		     ON Per."IdPersona" = Cli."IdPersona"
		  INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Por
		     ON Por."IdCliente" = Cli."IdCliente"
		    AND Por."IdPortafolio" = :p_IdPortafolio
		  INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocIden
		     ON DocIden."IdDocumentoIdentidad" = Per."IdDocumentoIdentidad"
		  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" AS ConsEje
		     ON ConsEje."IdPersonaEjecutivo" = Cli."IdPersonaEjecutivo"   
		  LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DirFisica
		     ON DirFisica."IdDireccionFisica" = Per."IdDireccionFisicaPrincipal"
		  LEFT JOIN "BSCL.Plataforma.BD::REG.DivisionPais" As DivPais
		     ON DivPais."IdDivisionPais" = DirFisica."IdDivisionPais"
		  LEFT JOIN "BSCL.Plataforma.BD::REG.Ciudad" As Ciudad
		     ON Ciudad."IdCiudad" = DirFisica."IdCiudad"
		  LEFT JOIN "BSCL.SocioNegocio.BD::PER.Telefono" AS Tel
		     ON Tel."IdTelefono" = Per."IdTelefonoPrincipal"
		  LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS DirElec
		     ON DirElec."IdDireccionElectronica" = Per."IdDireccionElectronicaPrincipal"
		  LEFT JOIN "BSCL.Orden.BD::ORD.Orden" AS Orden
		     ON Orden."IdCliente" = Cli."IdCliente"
		    and Orden."IdEmpresa" = :p_IdEmpresa 
		    AND Orden."IdPortafolioCliente" = Por."IdPortafolio"
		    AND TO_DATE(Orden."FechaOperacion") = :p_FechaIngresoSolicitud --'2020-08-21'
		    AND Orden."IdEstadoOrden" IN ('ING', 'AUT', 'OFE', 'EJE', 'PDA')
		LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarOrdenesUI" AS "OrdenesUI" ON "OrdenesUI"."IdOrden" = Orden."IdOrden"
		  LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" AS Asig
		     ON Asig."IdOrden" = Orden."IdOrden"
		  LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MvtoCmbte
		     ON MvtoCmbte."IdAsignacion" = Asig."IdAsignacion"  
		  LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosTotalizados" AS ConsMvtoCmbteCobroTot 
		     ON ConsMvtoCmbteCobroTot."IdMovimientoComprobante" = MvtoCmbte."IdMovimientoComprobante"  
		  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Empresa" AS Empresa 
		     ON Empresa."IdEmpresa" = Orden."IdEmpresa"   
		  Cross Join :v_logo As Logo   
	   WHERE Cli."IdCliente" = :p_IdCliente;
	   
	   SELECT COUNT(1) INTO v_Count FROM :v_DatosCliente;

		IF v_Count = 0 THEN			
			v_MensajeError := 'NO HAY DATOS PARA EL CLIENTE';
			SELECT
	               '' AS "NombreClienteCompleto",
	               '1-9' AS "DocumentoIdentidad",
	               0 AS "CodigoPortafolio",
	               '' AS "DireccionFisica", 
	               '' AS "Ciudad",
			       '' AS "Telefono",
			       '' AS "Email",
			       '0001-01-01' AS "FechaIngresoSolicitud",
			       '' AS "Nemotecnico",
			       '' AS "IdCondicionDeLiquidacion",
			       '' AS "Folio",
			       0 AS "Cantidad",
			       0 AS "PrecioOrden",
			       0 AS "MontoOrden",
			       '0001-01-01 00:00:00.000' AS "FechaRecepcion",
			       '0001-01-01 00:00:00.000' AS "FechaAceptacion",
			       '' AS "CodigoClaseOrden",
			       0 AS "FolioTransaccion",
			       0 AS "CantidadFacturacion",
			       0 AS "Precio",
			       0 AS "Monto",
			       '0001-01-01 00:00:00.000' AS "FechaMovimientoComprobante",
			       '' AS "IdBolsa",
			       0 AS "IdCorredoraContraparte",
			       '' AS "NombreAgente",
			       0 AS "Comision",
			       '' AS "NombreEmpresa",
				   '' AS "InstrumentoNegociacion",
				   '' AS "IdMoneda",			       
			       v_MensajeError AS "MensajeError"
			FROM DUMMY OPE
			CROSS JOIN :v_logo;			
		ELSE			
			SELECT *, 
				CASE 
					WHEN "Nemotecnico" IS NULL 
					THEN 'NO HAY ORDENES EN LA FECHA PARA ESTE CLIENTE'
					ELSE v_MensajeError
				END AS "MensajeError"
			FROM :v_DatosCliente;			
		END IF;
END;