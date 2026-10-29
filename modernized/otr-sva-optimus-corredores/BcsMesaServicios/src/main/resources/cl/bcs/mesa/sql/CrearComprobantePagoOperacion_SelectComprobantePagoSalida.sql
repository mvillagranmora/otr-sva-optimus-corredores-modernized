SELECT DISTINCT
	   cPag."IdTipoProducto" 
		, cPag."IdFormaDePago"
		, cPag."RecibePaga"
		, DICCIONARIO."Descripcion" AS "RecibePagaDesc"
		, cPag."IdModalidadPago"
		, cPag."IdMoneda" AS "IdMonedaComprobantePago"
		, cPag."Monto"
		, MP."Nombre" AS "NombreFormaDePago"
		, NULL AS "Dias"
		, fPag."PorDefecto"
		, fPag."RecibePagaPortafolio"
		, fPag."RecibePagaPortafolioDesc"
		, fPag."IdPreferenciaPago"
		, fPag."IdMedioDePago"
		, fPag."IdMoneda"
		, fPag."IdEstado"
		, fPag."NombreTipoOperacion"
		, fPag."NombreTipoProducto"
		, fPag."NombreMedioDePago"
		, fPag."NombreMoneda"
		, fPag."UsaCuentaBancaria"
		, fPag."UsaTarjetaBancaria"
		, fPag."NombreEstado"
		, cPag."IdCuentaBanco" AS "IdCuentaBancaria"
		, CBANC."IdBanco"
		, CBANC."IdTipoCuentaBancaria"
		, CASE PPP."IdCuentaBancaria" WHEN 0 THEN '' 
		        ELSE COALESCE(BANCBANC."NombreBanco",'')      
		    END AS "NombreBancoCuentaBancaria"
		, CASE PPP."IdCuentaBancaria" WHEN 0 THEN '' 
		        ELSE COALESCE(CBANC."NumeroCuentaBancaria",'')       
		    END AS "NumeroCuentaBancaria"    
FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" cPag
INNER JOIN "BSCL.Mesa.BD::MES.Operacion" ope ON (cPag."IdOperacion"= ope."IdOperacion")
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarFormasDePagoMesa" fPag
    ON (cPag."IdFormaDePago" = fPag."IdPreferenciaPago" AND
        cPag."IdTipoProducto" = fPag."IdTipoProducto" AND
        cPag."IdMoneda"= fPag."IdMoneda" AND
        fPag."IdEstado"='ACT')
INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" CONF
    ON ("CodigoParametro"='PLA.IdiomaSistema' AND
        cPag."IdEmpresa" = CONF."IdEmpresa")
LEFT JOIN (
        SELECT
            "CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado"
            ,CASE WHEN DIC."CodDiccionario"='UI_RECIBIMOS' THEN 'R' WHEN DIC."CodDiccionario"='UI_ENTREGAMOS' THEN 'P' ELSE '' END AS "RecibePaga"
        FROM "BSCL.Plataforma.BD::PLA.Diccionario" DIC
        WHERE DIC."CodDiccionario" in ('UI_RECIBIMOS','UI_ENTREGAMOS')
        ) DICCIONARIO
    ON (cPag."RecibePaga"= DICCIONARIO."RecibePaga" AND
        CONF."Valor" = DICCIONARIO."CodLenguaje")
LEFT JOIN "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" as PPP 
	ON (ope."IdPortafolio"=PPP."IdPortafolio" AND fPag."IdPreferenciaPago" = PPP."IdPreferenciaPago" AND fPag."IdEmpresa" = PPP."IdEmpresa" AND PPP."IdCuentaBancaria"=cPag."IdCuentaBanco" )
LEFT JOIN "BSCL.Plataforma.BD::PAG.PreferenciaPago" as
	PP ON (PPP."IdPreferenciaPago" = PP."IdPreferenciaPago")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TIPOP 
	ON (TIPOP."IdTipoOperacion" = PP."IdTipoOperacion")
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CLI 
	ON ope."IdCliente" = CLI."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CBANC 
	ON CLI."IdPersona" = CBANC."IdPersona" AND PPP."IdCuentaBancaria" = CBANC."IdCuentaBancaria"
LEFT JOIN (SELECT
	  "CuentaBanco"."IdCuentaBanco"
	 ,"CuentaBanco"."IdEmpresa"
	 ,"Empresa"."Nombre" AS "NombreEmpresa"
	 ,"CuentaBanco"."IdBanco"
	 ,"CuentaBanco"."NombreCuentaBancaria"
 	 ,'[' || "Banco"."NombreBanco" || '] N°' || "CuentaBanco"."NumeroCuentaBancaria" AS "NombreCuentaBancariaFull"
	 ,"CuentaBanco"."IdTipoCuentaBancaria"
	 ,"TipoCuentaBancaria"."NombreTipoCuentaBancaria"
	 ,"CuentaBanco"."IdMoneda"
	 ,"Moneda"."Nombre" AS "NombreMoneda"	 
	 ,"CuentaBanco"."NumeroCuentaBancaria"
	 ,"CuentaBanco"."Sucursal"
	 ,"CuentaBanco"."Direccion"
	 ,"CuentaBanco"."Contacto"
	 ,"CuentaBanco"."TelefonoContacto"
 	 ,"CuentaBanco"."EmailContacto"
	 ,"CuentaBanco"."IdEstado"
	 ,"CuentaBanco"."SaldoInicial"
	 ,"CuentaBanco"."AfectaPosicionInicial"
	 ,(CASE ("CuentaBanco"."AfectaPosicionInicial")
	 	WHEN 'S' THEN 'Si'
	 	WHEN 'N' THEN 'Especial'
	 	ELSE ''
	  END) AS "NombreAfectaPosicionInicial"
	 ,"CuentaBanco"."GeneraNomina"
	 ,(CASE "CuentaBanco"."GeneraNomina"
	 	WHEN 'S' THEN 'SI'
	 	WHEN 'N' THEN 'NO'
	 	ELSE ''
	  END) AS "NombreGeneraNomina"
	 ,"CuentaBanco"."GeneraTransferencia"
	 ,(CASE "CuentaBanco"."GeneraTransferencia"
	 	WHEN 'S' THEN 'SI'
	 	WHEN 'N' THEN 'NO'
	 	ELSE ''
	  END) AS "NombreGeneraTransferencia"
	 ,"CuentaBanco"."IdEmpleadoFirma1"
	  
	 ,"CuentaBanco"."MontoSegundaFirma"
	 
	 
	 ,"Estado"."Nombre" AS "NombreEstado"
	 ,"Banco"."NombreBanco"	 
 	 ,"Banco"."CodigoBanco"
	 ,"Banco"."IdPersona"
 	 ,"Banco"."IdTipoBanco"
     ,"Plaza"."Codigo"
	 ,"Plaza"."NombrePlaza"
	 ,COALESCE("Plaza"."PlazoRetencionMismoBanco",0) AS "PlazoRetencionMismoBanco"
	 ,COALESCE("Plaza"."PlazoRetencionOtroBanco",0) AS "PlazoRetencionOtroBanco"
	 ,COALESCE("Plaza"."PlazoMaximoCobroMismoBanco",0) AS "PlazoMaximoCobroMismoBanco"
	 ,COALESCE("Plaza"."PlazoMaximoCobroOtroBanco",0) AS "PlazoMaximoCobroOtroBanco"
	 ,COALESCE("Plaza"."IdCiudad",0) AS "IdCiudad"
	 ,"CuentaBanco"."IdPlaza"
	 ,"CuentaBanco"."CodigoCuentaInterbancario"
	 ,"CuentaBanco"."CodigoAba"
	 ,"CuentaBanco"."OtroCodigoBancoExtranjero"
	 ,"CuentaBanco"."CodigoIban"
	 ,"CuentaBanco"."OtroCodigoCuentaExtranjero",
	 (CASE ("CuentaBanco"."APV")
	 	WHEN 'S' THEN 'SI'
	 	WHEN 'N' THEN 'NO'
	 	ELSE ''
	  END) AS "RecaudacionExclusivaPAC"	 
FROM "BSCL.Tesoreria.BD::TES.CuentaBanco" AS "CuentaBanco"
INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS "Moneda" 
	ON ("Moneda"."IdMoneda" = "CuentaBanco"."IdMoneda")
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "Estado" 
	ON ("Estado"."IdEstado" = "CuentaBanco"."IdEstado")
INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" AS "Empresa"
	ON ("Empresa"."IdEmpresa" = "CuentaBanco"."IdEmpresa")
LEFT JOIN "BSCL.Plataforma.BD::BAN.Plaza" AS "Plaza" 
	ON ("Plaza"."IdPlaza" = "CuentaBanco"."IdPlaza")
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" AS "Banco" 
	ON ("Banco"."IdBanco" = "CuentaBanco"."IdBanco")
LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" AS "TipoCuentaBancaria" 
	ON ("TipoCuentaBancaria"."IdTipoCuentaBancaria" = "CuentaBanco"."IdTipoCuentaBancaria")) 	as CBANCCOR 
	ON PPP."IdCuentaBancaria" = CBANCCOR."IdCuentaBanco"
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" as BANCBANC ON (BANCBANC."IdBanco" = CBANC."IdBanco")
LEFT JOIN "BSCL.Plataforma.BD::PAG.PreferenciaPago" AS PP2  ON ( pp2."IdPreferenciaPago"= cPag."IdFormaDePago")
LEFT JOIN "BSCL.Plataforma.BD::PAG.MedioDePago"  AS MP ON  ( PP2."IdMedioDePago"= MP."IdMedioDePago")
WHERE 
	cPag."IdTipoProducto" = 'MERF' 
	AND cPag."IdOperacion" = :IdOperacion 
	AND cPag."RecibePaga"=:RecibePaga;