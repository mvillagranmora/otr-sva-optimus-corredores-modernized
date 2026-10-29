
-- Obtiene datos de la cuenta bancaria a modificar
SELECT		
		CTAB."IdCuentaBancaria",
		CTAB."IdPersona",
		CASE PER."Tipo" 
			WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"			
			ELSE PER."RazonSocial"
		END  AS "NombreCompleto",
		IDENT."DocumentoIdentidad",
		CTAB."EsPac",
		CTAB."Predeterminado",
		CTAB."IdBanco",
		CTAB."IdPlaza",
		CTAB."IdMoneda",
		CTAB."IdEstado",
		CTAB."CodigoAba",
		CTAB."CodigoIban",
		CTAB."IdentificadorCuenta",
		CTAB."IdPersonaRelacionada",
		CTAB."IdTipoCuentaBancaria",
		CTAB."NumeroCuentaBancaria",
		CTAB."CodigoCuentaInterbancario",
		CTAB."IdTipoIdentificadorCuenta",
		CTAB."OtroCodigoBancoExtranjero",
		CTAB."OtroCodigoCuentaExtranjero"
FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria"  AS CTAB
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = CTAB."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
WHERE  CTAB."IdCuentaBancaria" IN (:IdCuentaBancaria);