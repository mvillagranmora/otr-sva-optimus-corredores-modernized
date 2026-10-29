SET SCHEMA "BSCL";

DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias" AS

SELECT
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
	 ,"Empleado1Nombre"."NombreCompleto" AS "NombreEmpleadoFirma1"	 
	 ,"TCEMP1"."NombreTipoCargoEmpleado" AS "NombreTipoCargoEmpleadoFirma1"	 
	 ,"CuentaBanco"."MontoSegundaFirma"
	 ,"CuentaBanco"."IdEmpleadoFirma2"	 
	 ,"Empleado2Nombre"."NombreCompleto" AS "NombreEmpleadoFirma2"
	 ,"TCEMP2"."NombreTipoCargoEmpleado" AS "NombreTipoCargoEmpleadoFirma2"	 
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
	 ,"CuentaBanco"."OtroCodigoCuentaExtranjero"
	  ,(CASE ("CuentaBanco"."APV")
	 	WHEN 'S' THEN 'SI'
	 	WHEN 'N' THEN 'NO'
	 	ELSE ''
	  END) AS "RecaudacionExclusivaPAC"
						 
FROM "BSCL.Tesoreria.BD::TES.CuentaBanco" AS "CuentaBanco"
INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS "Moneda" ON ("Moneda"."IdMoneda" = "CuentaBanco"."IdMoneda")
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "Estado" ON ("Estado"."IdEstado" = "CuentaBanco"."IdEstado")
INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" AS "Empresa" ON ("Empresa"."IdEmpresa" = "CuentaBanco"."IdEmpresa")
LEFT JOIN "BSCL.Plataforma.BD::BAN.Plaza" AS "Plaza" ON ("Plaza"."IdPlaza" = "CuentaBanco"."IdPlaza")
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" AS "Banco" ON ("Banco"."IdBanco" = "CuentaBanco"."IdBanco")
LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" AS "TipoCuentaBancaria" ON ("TipoCuentaBancaria"."IdTipoCuentaBancaria" = "CuentaBanco"."IdTipoCuentaBancaria")
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS "Empleado1" ON ("Empleado1"."IdEmpleado" = "CuentaBanco"."IdEmpleadoFirma1")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" AS "Empleado1Nombre" ON ("Empleado1"."IdPersona" = "Empleado1Nombre"."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" AS "TCEMP1" ON ("TCEMP1"."CodigoTipoCargoEmpleado" = "Empleado1"."CodigoTipoCargoEmpleado")
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS "Empleado2" ON ("Empleado2"."IdEmpleado" = "CuentaBanco"."IdEmpleadoFirma2")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" AS "Empleado2Nombre" ON ("Empleado2"."IdPersona" = "Empleado2Nombre"."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" AS "TCEMP2" ON ("TCEMP2"."CodigoTipoCargoEmpleado" = "Empleado2"."CodigoTipoCargoEmpleado")

WITH READ ONLY;

GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
