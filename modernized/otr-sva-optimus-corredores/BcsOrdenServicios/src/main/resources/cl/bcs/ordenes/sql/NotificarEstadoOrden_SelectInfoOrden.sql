/**************************************************************
Proposito	: Script consulta info de la orden
autor		: RU
fecha		: 01/02/2017
***********************************************************/

SELECT
			O."IdOrden",
			O."IdOrden" AS "FOLIO", 
			O."IdCliente", 
			CC."IdPersona" AS "IDCLIENTEPERSONA",
			O."Nemotecnico" AS "NEMO",
			CC."DocumentoIdentidad" AS "RUT",
			CC."DocumentoIdentidadAdicional" AS "RUTCAVALI",
			CC."NombreCompleto" AS "CLIENTE", 
			O."IdPortafolioCliente", 
			CP."DescripcionPortafolio" AS "CUENTA", 
			O."IdEmpleadoDigitador", 
			CE."NombreCompleto" AS "AGENTE",
			CE."IdPersona" AS "IDEMPLEADOPERSONA",
			CE."IdDireccionElectronicaPrincipal" AS "IDCORREOEMPLEADO",
			COALESCE(CPDE."Direccion",'') AS "CORREO",
			COALESCE(CPT."Numero",'') AS "TELEFONO",
			TO_VARCHAR(ADD_SECONDS(CURRENT_UTCTIMESTAMP,:SegundosZonaHoraria), 'DD-MM-YYYY') AS "FECHA",
			TO_VARCHAR(ADD_SECONDS(CURRENT_UTCTIMESTAMP,:SegundosZonaHoraria), 'HH24:MI:SS') AS "HORA",
			CTP."Nombre" AS "PRODUCTO",
			CTO."Nombre" AS "TIPO_OPERACION",
			"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(O."Cantidad",'i',0) AS "CANTIDAD",
			O."PrecioOrden" AS "PRECIO",
			"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(O."MontoOrden",CASE WHEN O."IdMonedaOrden"='CLP' THEN 'i' ELSE 'd' END,CASE WHEN O."IdMonedaOrden"='CLP' THEN 0 ELSE 3 END ) AS "MONTO",
			O."IdMonedaOrden" AS "MONEDA",
			CES."Nombre" AS "ESTADO",
			CCDG."RazonSocial" AS "CORREDORA",
            CCDG."Telefono" AS "ATENCIONCLIENTE",
			CDL."Nombre" AS "CONDICIONLIQUIDACION",
			TPO."Nombre" AS "TIPOPRECIOORDEN",
			TO_VARCHAR(O."FechaVigencia",'DD-MM-YYYY') AS "FECHAVIGENCIA",
			CASE 
				WHEN IFNULL(O."EsVentaCorta",'N') = 'S' THEN O."TasaVentaCorta"
				WHEN IFNULL(O."TasaSimultanea",0) <> 0  THEN O."TasaSimultanea"
				WHEN IFNULL(O."TasaPromedioReporte",0) <> 0  THEN O."TasaPromedioReporte"
				ELSE 0
			END AS "TASA",
			
			"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(
				CASE 
					WHEN IFNULL(O."EsVentaCorta",'N') = 'S' THEN IFNULL(O."PlazoVentaCorta",0)
					WHEN IFNULL(O."TasaSimultanea",0) <> 0  THEN IFNULL(O."PlazoSimultanea",0)
					WHEN IFNULL(O."TasaPromedioReporte",0) <> 0 THEN IFNULL(O."PlazoReporte",0)
					ELSE 0
				END 
			,'e',0) AS "PLAZOVENCIMIENTO", 
			"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(O."MontoPrimaVentaCorta",CASE WHEN O."IdMonedaOrden"='CLP' THEN 'i' ELSE 'd' END, CASE WHEN O."IdMonedaOrden"='CLP' THEN 0 ELSE 3 END ) AS "MONTOPRIMA",
			O."EsVentaCorta" AS "VENTACORTA",
			O."IdTipoInstrumento" AS "IDTIPOINSTRUMENTO"
			
			,CE."DocumentoIdentidad" AS "DocumentoIdentidadEmpleado" 
			,CP."CodigoPortafolio"
		FROM "BSCL.Orden.BD::ORD.Orden" O
		INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" CP ON CP."IdPortafolio" = O."IdPortafolioCliente"
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC ON CC."IdCliente" = CP."IdCliente"
		

		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE ON CE."IdEmpleado" = CP."IdEmpleadoEjecutivo"
		
		
		INNER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" CDL ON CDL."IdCondicionDeLiquidacion" = O."IdCondicionDeLiquidacion"
		INNER JOIN "BSCL.Orden.BD::ORD.TipoPrecioOrden" TPO ON TPO."IdTipoPrecioOrden" = O."IdTipoPrecio"
		
		
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE ON CPDE."IdDireccionElectronica" = CE."IdDireccionElectronicaPrincipal"
		
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" CPT ON CPT."IdTelefono" = CE."IdTelefonoPrincipal"
		INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProducto" CTP ON CTP."IdTipoProducto" = O."IdProducto"
		INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" CTO ON CTO."IdTipoOperacion" = O."IdTipoOperacion" AND CTO."IdTipoInstrumento" = O."IdTipoInstrumento"
		INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" CES ON CES."IdEstado" = O."IdEstadoOrden"
		INNER JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" CCDG ON CCDG."IdEmpresa" = :IdEmpresa
		WHERE O."IdEmpresa" = :IdEmpresa AND O."IdOrden" IN ( :ListaIdOrden )

