SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Custodia.CrystalReports.Servicios::CartaTraspaso";
GO

--
--  Proposito: servicio para consultar informacion para carta de traspaso.
--  Fecha Creacion: 28/12/2017
--

CREATE PROCEDURE "BSCL.Custodia.CrystalReports.Servicios::CartaTraspaso"(
	-- Parametros entrada 
	IN p_IdEmpresa		BIGINT,
	-- Parametros entrada del servicio privado
	IN p_Movimientos 	NVARCHAR(5000)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER  
DEFAULT SCHEMA "BSCL"
--READS SQL DATA   
AS
BEGIN	
	
	
	DECLARE Movimientos BIGINT ARRAY;
	DECLARE Contador   INTEGER;
	DECLARE Cadena     NVARCHAR(5000);
		
		
		-- se crea tabla de movimientos a travez de cadena de caracteres enviada 
		
	Contador := 1;
	Cadena := p_Movimientos;
		
	WHILE LOCATE(:Cadena,',')>0 DO
		Movimientos[:Contador]:=SUBSTR_BEFORE(:Cadena,',');
		Cadena := SUBSTR_AFTER(:Cadena,',');
		Contador:=Contador +1;
	END WHILE;
		Movimientos[:Contador]:=:Cadena;
		
		v_Movimientos = UNNEST(:Movimientos)as("IdMovimiento");
		
	
	v_MovimientoCustodia = SELECT
										M."IdMovimientoCarteraEnTransito"
										,CASE WHEN M."IdTipoOperacion" = 'INGTIT'
										 THEN 'Compra'  ELSE
										 	  'Venta' END AS "TipoOperacio"	
										 ,CASE WHEN M."IdTipoOperacion" = 'INGTIT'THEN EM."RazonSocial" ELSE M."NombrePersona" END AS "Vendedor"
										 ,CASE WHEN M."IdTipoOperacion" = 'RETTIT'THEN EM."RazonSocial" ELSE M."NombrePersona" END AS "Comprador"
										 
										 ,CASE WHEN M."IdTipoOperacion" = 'INGTIT'THEN EM."RUT" ELSE M."DocumentoIdentidad" END AS "RutVendedor"
										 ,CASE WHEN M."IdTipoOperacion" = 'RETTIT'THEN EM."RUT" ELSE M."DocumentoIdentidad" END AS "RutComprador"
										 	
										,M."NombrePersona"
										,M."DocumentoIdentidad"
										,M."CodigoPortafolio"
										,M."Cantidad"
										,TO_NVARCHAR(M."FechaIngresoRegistro",'DD-MM-YYYY HH24:MI:SS') AS "FechaIngresoRegistro"
										,0 AS "IdCartera"
										,M."PrecioCompra"
										,M."Nemotecnico"
										,M."IdEmisor"
										,M."NombreEmisor" AS "Instrumento" 
										,COALESCE (M."NombreBoveda" ,'DCV') AS "NombreBoveda"
										,TO_NVARCHAR(M."FechaOperacion",'DD-MM-YYYY') AS "FechaOperacion"
										,M."IdConcepto"
										,M."NombreConcepto"
										,CONT."NombreCompleto" AS "NombreEntidadDestino"
										,CPT."Anexo" || ' ' || CPT."Numero" AS "Telefono"
										,M."IdMonedaInstrumento"
										,M."IdCliente"
										,M."IdPortafolio"
										,EM."RazonSocial" AS "RazonCorredora"
							   			,EM."Direccion" AS "DireccionCorredora"
										,EM."RUT" AS "RutCorredora"
										,EM."Email" AS "EmailCorredora"
										,CPDF."NombreCiudad" AS "NombreCiudadComprador"
										,CPDF."Calle" AS "CalleComprador"
										,CPDF."Numero" AS "NumeroComprador"
										,CPDF."NombreDivisionPais" AS "ComunaComprador"
										,CPDF."NombrePais" AS "PaisComprador"
										,CE1."NombreCompleto" AS "NombreEjecutivo"
										,CE1."DocumentoIdentidad" AS "RutEjecutivo"
		FROM :v_Movimientos MV
		JOIN  "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEnTransito" M ON M."IdMovimientoCarteraEnTransito"= MV."IdMovimiento"
		LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarConceptosCustodia" C ON M."IdConcepto" = C."IdConcepto" AND C."NombreEntidad" = 'CUS.MOVIMIENTOCUSTODIA'
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte" CONT ON C."IdContraparte" = CONT."IdContraparte" AND CONT."IdEstado" = 'ACT'		
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC ON (CC."IdPersona" = M."IdPersona")
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" CPDF ON CPDF."IdDireccionFisica" =  CC."IdDireccionFisicaPrincipal"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" CPT ON CPT."IdTelefono" =  CC."IdTelefonoPrincipal"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" EM ON EM."IdEmpresa" = M."IdEmpresa"
		LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" U1 ON U1."IdUsuario" = M."IdAgente"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE1 ON U1."IdEntidad" = CE1."IdEmpleado" AND U1."TipoEntidad" = 'EMP';
		
	v_logo =
		SELECT TOP 1 "Binario" AS "LogoCorredora"
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;

	v_MovimientoCustodiaFinal = SELECT '1' AS "Pagina", * FROM :v_MovimientoCustodia
								UNION ALL
								SELECT '2' AS "Pagina", * FROM :v_MovimientoCustodia;
		
	--Salida del SP
	SELECT * FROM :v_MovimientoCustodiaFinal
	CROSS JOIN :v_logo;
	
END;
GO