SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Custodia.CrystalReports.Servicios::TraspasoCustodiaEmisor";
GO
CREATE PROCEDURE "BSCL.Custodia.CrystalReports.Servicios::TraspasoCustodiaEmisor"(
	IN p_IdEmpresa		BIGINT,
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
	DECLARE v_moneda NVARCHAR(5);
    DECLARE v_cantidad_texto NVARCHAR(1000);
	DECLARE v_cantidad DECIMAL(26,8);
	
		
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
/*
SI INGRESO DE TITULO
COMPRA = CORREDORA
VENDE = CLIENTE
SI NO (EGRESO DE TITULO)
COMPRA = CLIENTE
VENDE = CORREDORA

*/
	v_MovimientoCustodia = SELECT
										M."IdTipoOperacion"
										 ,CASE WHEN M."IdTipoOperacion" = 'INGTIT'THEN M."NombrePersona" ELSE EM."RazonSocial"  END AS "Vendedor"
										 ,CASE WHEN M."IdTipoOperacion" = 'INGTIT'THEN EM."RazonSocial" ELSE M."NombrePersona" END AS "Comprador"								 
										 ,CASE WHEN M."IdTipoOperacion" = 'INGTIT'THEN M."DocumentoIdentidad" ELSE  EM."RUT" END AS "RutVendedor"
										 ,CASE WHEN M."IdTipoOperacion" = 'INGTIT'THEN EM."RUT" ELSE M."DocumentoIdentidad"   END AS "RutComprador"
										,TO_NVARCHAR(M."FechaIngresoRegistro",'DD-MM-YYYY') AS "FechaIngresoRegistro"
										,M."PrecioCompra"
										,M."IdMonedaInstrumento"
										,M."Cantidad"
										,INS."RazonSocial" AS "RazonSocialEmisor" 
										,M."Nemotecnico" AS "Instrumento"  
										,M."FechaOperacion" AS "FechaOperacion"
										,CASE WHEN M."IdTipoOperacion" = 'INGTIT'THEN EM."Direccion" ELSE CPDF."Calle"|| ', ' ||CPDF."Numero"|| ', ' ||CPDF."NombreCiudad"|| ', ' ||CPDF."NombreDivisionPais"|| ', ' ||CPDF."NombrePais"|| '.' || CPDF."CodigoPostal"  END AS "DireccionComprador"
										,CASE WHEN M."IdTipoOperacion" = 'INGTIT'THEN '' ELSE CPDF."NombrePais"  END AS "NacionalidadComprador"
										,CASE WHEN M."IdTipoOperacion" = 'INGTIT'THEN EM."Email" ELSE CPDE."Direccion"   END AS "EmailComprador"
										,CASE WHEN M."IdTipoOperacion" = 'INGTIT'THEN EM."Telefono" ELSE CPT."Anexo" || ' ' || CPT."Numero"   END AS "TelefonoComprador"
										,CASE WHEN M."IdTipoOperacion" = 'INGTIT'THEN '' ELSE CYG."NombreCompleto" END AS "NombreConyugeComprador"
										,CASE WHEN M."IdTipoOperacion" = 'INGTIT'THEN '' ELSE TEC."Nombre" END AS "EstadoCivilComprador"	
										,CASE WHEN M."IdTipoOperacion" = 'INGTIT'THEN PER."NombreCompleto" ELSE 'FIRMA DEL VENDEDOR O  SU REPRESENTANTE' END AS "RepresentanteLegalVenta"
										,CASE WHEN M."IdTipoOperacion" = 'INGTIT'THEN 'FIRMA DEL VENDEDOR O  SU REPRESENTANTE' ELSE PER."NombreCompleto"   END AS "RepresentanteLegalCompra"
										,CASE WHEN M."IdTipoInstrumento" = 'OSA' THEN 'Opciones' ELSE 'Acciones' END AS "TipoTraspaso"									
		FROM :v_Movimientos MV
		JOIN  "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEnTransito" M ON M."IdMovimientoCarteraEnTransito"= MV."IdMovimiento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = M."IdInstrumento"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC ON (CC."IdPersona" = M."IdPersona")
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" CPDF ON CPDF."IdDireccionFisica" =  CC."IdDireccionFisicaPrincipal"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE ON CPDE."IdDireccionElectronica" =  CC."IdDireccionElectronicaPrincipal"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" CPT ON CPT."IdTelefono" =  CC."IdTelefonoPrincipal"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" EM ON EM."IdEmpresa" = M."IdEmpresa"
		LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" U1 ON U1."IdUsuario" = M."IdAgente"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE1 ON U1."IdEntidad" = CE1."IdEmpleado" AND U1."TipoEntidad" = 'EMP'
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" CYG ON CYG."IdPersona" = CC."IdPersonaConyuge"
		LEFT JOIN "BSCL.Plataforma.BD::REG.TipoEstadoCivil" TEC ON CC."EstadoCivil" = TEC."IdTipoEstadoCivil"
	    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON PER."IdPersona" = CC."IdPersonaRepresentante"	;
	
	SELECT "Cantidad", "IdMonedaInstrumento" INTO v_cantidad, v_moneda FROM :v_MovimientoCustodia;
	CALL "BSCL.Plataforma.BD.Servicios::ConvertirMontoEscrito"(v_cantidad,'CL', v_moneda, :v_cantidad_texto);
    v_cantidad_t = select (v_cantidad_texto || '.') AS "CantidadTexto" FROM DUMMY;
	v_logo =
		SELECT TOP 1 "Binario" AS "LogoCorredora"
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;
		
	--Salida del SP
	SELECT * FROM :v_MovimientoCustodia
	CROSS JOIN :v_logo
    CROSS JOIN :v_cantidad_t;
	
END;
GO
/*
CALL "BSCL.Custodia.CrystalReports.Servicios::TraspasoCustodiaEmisor"
(
     p_IdEmpresa 	 => 1
     ,p_Movimientos => 18872
);
*/


