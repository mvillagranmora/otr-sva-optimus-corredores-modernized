/********************************************************** 
Proposito	: Ajustar Fecha del Sistema
autor		: SHECHENLEITNER - BCS
fecha		: 24/05/2017
***********************************************************/
UPDATE "BSCL.Plataforma.BD::PLA.Configuracion" 
SET "Valor" = :FechaSistema
WHERE "CodigoParametro" = 'PLA.FechaSistema'
AND "IdEmpresa" = :IdEmpresa