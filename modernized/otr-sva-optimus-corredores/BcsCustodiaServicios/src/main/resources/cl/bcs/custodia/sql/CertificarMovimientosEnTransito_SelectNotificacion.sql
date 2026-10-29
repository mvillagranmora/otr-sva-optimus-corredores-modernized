with 
	usuario as (
		SELECT 
				CE."IdPersona", 
				CE."IdDireccionElectronicaPrincipal"
		FROM "BSCL.Plataforma.BD::ACC.Usuario" U
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado"  CE ON CE."IdEmpleado" = U."IdEntidad"  
		WHERE "IdUsuario" = :IdUsuario
	)
		SELECT DISTINCT
				CCL."IdPersona" AS "IdPersonaCLI", 
				CCL."IdDireccionElectronicaPrincipal" AS "IdDireccionElectronicaPrincipalCLI", 
				CCL."NombreCompleto", 
				DI."DocumentoIdentidad" AS "DI", 
				DI."IdTipoDocumentoIdentidad" AS "TipoDoc",
				MCT."IdMovimientoCarteraEnTransito", 
				MCT."IdTipoOperacion" AS "Operacion",
				MCT."Cantidad",
				MCT."PrecioCompra",
				MCT."MontoCompra",
				MCT."NemoSVS",
				MCT."IdMonedaSistema" AS "TipoMoneda",
				MCT."IdPortafolio",
				MCT."Nemotecnico" AS "Instrumento",
				COALESCE(DE."Direccion", CPGDEG."Direccion") AS "Correo",
				CPTO."Nombre" AS "NombreOperacion", 
				CPT."Numero" AS "NumeroTel",
				CE."NombreCompleto" AS "Agente",
				CCP."CodigoPortafolio",
				CCDG."RazonSocial" AS "Corredora",
				CCDG."Telefono" AS "AtencionCliente",
				US."IdPersona",
				US."IdDireccionElectronicaPrincipal"
		FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CCL
		INNER JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" MCT ON MCT."IdCliente" = CCL."IdCliente"
		INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" CCP ON CCP."IdPortafolio" = MCT."IdPortafolio"
     	INNER JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" CCDG ON CCDG."IdEmpresa" = :IdEmpresa	
		INNER JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" DE ON DE."IdPersona" = CCL."IdPersona" AND DE."IdDireccionElectronica" = CCL."IdDireccionElectronicaPrincipal"
     	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaGrupoDireccionElectronicaGlobal" CPGDEG ON CPGDEG."IdGrupo" = CCL."IdDireccionElectronicaPrincipal"
     	INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON DI."IdDocumentoIdentidad" = CCL."IdDocumentoIdentidad"
 		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" CPT ON CPT."IdTelefono"=CCL."IdTelefonoPrincipal"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" CPTO ON MCT."IdConcepto" = CPTO."IdConcepto"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE ON CE."IdEmpleado" = CCP."IdEmpleadoEjecutivo"
		, usuario AS US
		WHERE MCT."IdMovimientoCarteraEnTransito" IN (:idsMovs)
