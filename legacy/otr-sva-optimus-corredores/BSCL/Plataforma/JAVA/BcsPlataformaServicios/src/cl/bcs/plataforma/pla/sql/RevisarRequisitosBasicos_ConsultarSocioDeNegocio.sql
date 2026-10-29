/********************************************************** 
Proposito	: consulta para validar empresa y socio corredora
autor		: RU
fecha		: 07/02/2017
***********************************************************/
  SELECT EM."IdEmpresa", EM."IdSocioDeNegocio" AS "IdSocioDeNegocioEmpresa", CSN."IdSocioDeNegocio", CSN."IdContraparte",CSN."IdCliente"
  FROM "BSCL.Plataforma.BD::PLA.Empresa" AS EM
  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS CSN ON CSN."IdSocioDeNegocio" = EM."IdSocioDeNegocio"
  WHERE EM."IdEmpresa" = :IdEmpresa;