with 
	usuario as (
		SELECT 
				CE."IdPersona", 
				CE."IdDireccionElectronicaPrincipal"
		FROM "BSCL.Plataforma.BD::ACC.Usuario" U
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado"  CE ON CE."IdEmpleado" = U."IdEntidad"  
		WHERE "IdUsuario" = :IdUsuario
	)
		SELECT 
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
				MCT."IdMonedaSistema" AS "TipoMoneda",
				MCT."IdPortafolio",
				MCT."Nemotecnico" AS "Instrumento",
				CPGDEG."Direccion" AS "Correo",
				CPTO."Nombre" AS "NombreOperacion", 
				CPT."Numero" AS "NumeroTel",
				CEMP."Nombre" AS "Corredora",	
				US.*
		FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CCL
     	INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" CEMP ON CEMP."IdEmpresa" = :IdEmpresa
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaGrupoDireccionElectronicaGlobal" CPGDEG ON CPGDEG."IdGrupo" = CCL."IdDireccionElectronicaPrincipal"		
		INNER JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" MCT ON MCT."IdCliente" = CCL."IdCliente"
 		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" CPT ON CPT."IdPersona"=CCL."IdPersona"
		INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON DI."IdDocumentoIdentidad" = CCL."IdDocumentoIdentidad"	
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" CPTO ON MCT."IdConcepto" = CPTO."IdConcepto"
		--LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" 
		, usuario AS US
		WHERE MCT."IdMovimientoCarteraEnTransito" IN (:idsMovs)