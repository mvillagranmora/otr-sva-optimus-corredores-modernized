SET SCHEMA "BSCL";

--  Proposito: Consolidación Registro Historico de cliente
--  Autor: JSALINAS - BCS
--  Fecha Creacion: 29/03/2016
--* --------------------------------------------------------------
--* Fecha ....: Abril 2018
--* Autor ....: MSANDOVAL - BCS
--* Proposito : Optimización y corrección de Servicio.
--* --------------------------------------------------------------
GO
DROP PROCEDURE  "BSCL.Normativa.BD.Servicios.NCG380::ConsolidaClientes";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.NCG380::ConsolidaClientes"() 
	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER
	DEFAULT SCHEMA "BSCL" 
	
AS BEGIN

	DECLARE v_SecuenciaTablaGlobal BIGINT DEFAULT 0;
	DECLARE v_FecSistema DATE;	

    SELECT 	Top 1 "Valor" 
    INTO	v_FecSistema 
    FROM	"BSCL.Plataforma.BD::PLA.Configuracion" 
    WHERE	"CodigoParametro" = 'PLA.FechaSistema';	

	--* Obtengo el registro de Hoy
	--* ----------------------------------------------------------------------------------------------------------
	CALL "BSCL.Normativa.BD.Servicios.NOR::ConsultarClienteOnline"( IdSecuenciaGlobal => :v_SecuenciaTablaGlobal );
	--* ----------------------------------------------------------------------------------------------------------

	v_Cliente_Por_Fecha = SELECT "IdCliente", MAX("FechaRegistro") AS "Max_FechaRegistro" 
					FROM "BSCL.Normativa.BD::NOR.NCG380RegistroHistoricoCliente" T1 
					GROUP BY "IdCliente";
	--* ULTIMO REGISTRO DE CLIENTE (ULTIMOS DATOS GUARDADOS)
	v_TablaUtlReg_x_Cli = 	SELECT T1."IdCliente", T1."Concatenados" 
							FROM "BSCL.Normativa.BD::NOR.NCG380RegistroHistoricoCliente" T1
							INNER JOIN :v_Cliente_Por_Fecha T2 ON T1."IdCliente" = T2."IdCliente" AND T1."FechaRegistro" = T2."Max_FechaRegistro";


	--* POR MIENTRAS LA VISTA SE CORRIGE (ACTUALMENTE ENTREGA 2 VECES MISMO REGUISTRO)
	v_Tabla_ONLINE = SELECT DISTINCT * FROM "BSCL.Normativa.BD::NOR.GL_SALIDA_CLIENTE_ONLINE" WHERE "IdClienteOnline" = v_SecuenciaTablaGlobal; 


	--* CLIENTES NUEVOS
	v_TablaNuevos = SELECT HOY."NumeroCliente"
					FROM :v_Tabla_ONLINE HOY
					WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::NOR.NCG380RegistroHistoricoCliente" T2 WHERE HOY."NumeroCliente" = T2."IdCliente");


	--* CLIENTES QUE HAN CAMBIADO
	v_TablaCambios = SELECT  HOY."NumeroCliente",HOY."Concatenados"
					FROM :v_Tabla_ONLINE HOY 
					EXCEPT 
					SELECT  HIST."IdCliente" , HIST."Concatenados" 
					FROM :v_TablaUtlReg_x_Cli HIST;


	--* TODOS ESTOS DEBEN INGRESARSE AL HISTORICO
	v_TablaFinal = 	SELECT DISTINCT "NumeroCliente" FROM :v_TablaNuevos 
				 	UNION ALL
					SELECT DISTINCT "NumeroCliente" FROM :v_TablaCambios;


	INSERT INTO "BSCL.Normativa.BD::NOR.NCG380RegistroHistoricoCliente"
	(	"IdRegistroHistoricoCliente", "IdCliente", "DocumentoIdentidadRut", "DocumentoIdentidadDig", "DocumentoIdentidadPasaporte",
		"Glei", "ApellidoPaterno", 	"ApellidoMaterno", "PrimerNombre", "SegundoNombre",
		"RazonSocial", "Telefono", "DireccionElectronica", "TipoDireccion", "Calle",
		"Numero", "Departamento", "Comuna", "Ciudad", "Provincia",
		"Pais", "CodigoPostal", "Nacionalidad", "RelacionEntidad", "ContratoAdminCartera",
		"EmpleadorRut", "EmpleadorDig", "EmpleadorRazonSocial", "EstadoCivil", "ConyugeRut",
		"ConyugeDig", "ConyugePasaporte", "ConyugeApellPaterno", "ConyugeApellMaterno", "ConyugePrimerNombre",
		"ConyugeSegundoNombre", "ConyugeRazonSocial", "ConyugeTelefono", "ConyugeDireccionElectronica", "ConyugeTipoDireccion",
		"ConyugeCalle", "ConyugeNumero", "ConyugeDepartamento", "ConyugeComuna", "ConyugeCiudad",
		"ConyugeProvincia", "ConyugePais", "ConyugeCodigoPostal", "ConyugeNacionalidad", "ConyugeRelacionEntidad",
		"RepLegalRut", "RepLegalDig", "RepLegalPasaporte", "RepLegalGlei", "RepLegalApellPaterno",
		"RepLegalApellMaterno", "RepLegalPrimerNombre", "RepLegalSegundoNombre", "RepLegalRazonSocial", "RepLegalTelefono",
		"RepLegalDireccionElectronica", "RepLegalTipoDireccion", "RepLegalCalle", "RepLegalNumero", "RepLegalDepartamento",
		"RepLegalComuna", "RepLegalCiudad", "RepLegalProvincia", "RepLegalPais", "RepLegalCodigoPostal",
		"RepLegalNacionalidad", "RepLegalRelacionEntidad", "RepLegalEstadoCivil", "ConyRepLegalRut", "ConyRepLegalDig",
		"ConyRepLegalPasaporte", "ConyRepLegalApellPaterno", "ConyRepLegalApellMaterno", "ConyRepLegalPrimerNombre", "ConyRepLegalSegundoNombre",
		"ConyRepLegalRazonSocial", "ConyRepLegalTelefono", "ConyRepLegalDireccionElectronica", "ConyRepLegalTipoDireccion", "ConyRepLegalCalle",
		"ConyRepLegalNumero", "ConyRepLegalDepartamento", "ConyRepLegalComuna", "ConyRepLegalCiudad", "ConyRepLegalProvincia",
		"ConyRepLegalPais", "ConyRepLegalCodigoPostal", "ConyRepLegalNacionalidad", "ConyRepLegalRelacionEntidad", "CuentaBancariaUno",
		"CuentaBancariaDos", "CuentaBancariaTres", "ClaseOrden", "PerfilActual", "PerfilAnterior",
		"FechaInicioPerfilActual", "EstadoRegistro",  "FechaRegistro", "Concatenados"
	) ( SELECT 	"BSCL.Normativa.BD.Secuencias::ClienteHistorico".NEXTVAL AS "IdRegistroHistoricoCliente", HOY."NumeroCliente", "Rut", "Dig", "Pasaporte",
				"Glei", "ApellidoPaterno", "ApellidoMaterno", "PrimerNombre", "SegundoNombre",
				"RazonSocial", "Telefono", "Email", "DireccionTipo", "DireccionNombre",   
				"DireccionNumero", "DireccionDepto", "DireccionComuna", "DireccionCiudad", "DireccionEstado",   
				"DireccionPais", "CodigoPostal", "Nacionalidad", "RelacionIntermediario", "AdministracionCartera",	 	
				"RutEntidadEmpleadora", "DigEntidadEmpleadora", "RazonSocialEntidadEmpleadora",	"EstadoCivil", "ConyRut", 
				"ConyDig", "ConyPasaporte", "ConyApellidoPaterno", "ConyApellidoMaterno", "ConyPrimerNombre", 
				"ConySegundoNombre", "ConyRazonSocial", "ConyTelefono", "ConyEmail", "ConyDireccionTipo",	
				"ConyDireccionNombre", "ConyDireccionNumero", "ConyDireccionDepto", "ConyDireccionComuna", "ConyDireccionCiudad",
				"ConyDireccionEstado", "ConyDireccionPais", "ConyCodigoPostal", "ConyNacionalidad", "ConyRelacionIntermediario",
				"RepRut", "RepDig", "RepPasaporte", "RepGlei", "RepApellidoPaterno",
				"RepApellidoMaterno", "RepPrimerNombre", "RepSegundoNombre", "RepRazonSocial", "RepTelefono",
				"RepEmail", "RepDireccionTipo", "RepDireccionNombre", "RepDireccionNumero", "RepDireccionDepto",	
				"RepDireccionComuna", "RepDireccionCiudad", "RepDireccionProvincia", "RepDireccionPais", "RepCodigoPostal",
				"RepNacionalidad", "RepRelacionIntermediario", "RepEstadoCivil", "ConyRepRut", "ConyRepDig",
				"ConyRepPasaporte",	"ConyRepApellidoPaterno", "ConyRepApellidoMaterno", "ConyRepPrimerNombre", "ConyRepSegundoNombre",	
				"ConyRepRazonSocial", "ConyRepTelefono", "ConyRepEmail", "ConyRepDireccionTipo", "ConyRepDireccionNombre",
				"ConyRepDireccionNumero", "ConyRepDireccionDepto", "ConyRepDireccionComuna", "ConyRepDireccionCiudad", "ConyRepDireccionEstado",
				"ConyRepDireccionPais", "ConyRepCodigoPostal", "ConyRepNacionalidad", "ConyRepRelacionIntermediario", "CuentaBancariaUno",	
				"CuentaBancariaDos", "CuentaBancariaTres", "ClaseOrden", "Perfil", "PerfilAnterior",
				"FechaInicioPerfilActual", 'V', v_FecSistema, HOY."Concatenados"
		FROM 	:v_TablaFinal NUEVOS
		INNER JOIN :v_Tabla_ONLINE HOY ON ( HOY."NumeroCliente" = NUEVOS."NumeroCliente" ) 
		);
			
END;