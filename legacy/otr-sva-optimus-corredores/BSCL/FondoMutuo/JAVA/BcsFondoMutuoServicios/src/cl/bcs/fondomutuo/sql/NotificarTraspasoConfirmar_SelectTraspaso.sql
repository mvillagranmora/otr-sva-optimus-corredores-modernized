SELECT 
 USU."IdPersona"
,PER."IdDireccionElectronicaPrincipal"
,USU."NombreCompleto"
,CURRENT_UTCTIMESTAMP 		AS "FechaCreacion"
,CCDG."RazonSocial" AS "Corredora"
,CCDG."Telefono" AS "AtencionCliente"
FROM 	
(	
SELECT 
		 TRASPASO."IdTraspaso"
	    ,TRASPASO."IdEmpresa"
		,TRASPASO."IdUsuario"
	    ,CONFIG."FechaSistema"
	    ,TRASPASO."FechaIngreso"
	    ,DAYS_BETWEEN (TO_DATE (TRASPASO."FechaIngreso", 'YYYY-MM-DD'), TO_DATE(CONFIG."FechaSistema", 'YYYY-MM-DD')) "DiasTranscurridos"
	FROM "BSCL.FondoMutuo.BD::TRA.Traspaso" AS TRASPASO
	INNER JOIN (
				SELECT  "Valor" AS "FechaSistema"
			 FROM "BSCL.Plataforma.BD::PLA.Configuracion"
			 WHERE "IdEmpresa"=:IdEmpresa AND "CodigoParametro" = 'PLA.FechaSistema'
	) AS CONFIG ON (1=1)
	WHERE TRASPASO."IdEmpresa"=:IdEmpresa AND TRASPASO."IdEstado"='ING'
) AS TRAS
INNER JOIN  "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" AS USU ON (USU."IdUsuario"=TRAS."IdUsuario" AND TRAS."IdEmpresa"=USU."IdEmpresa" AND USU."IdEstado"='ACT')
INNER JOIN 	"BSCL.SocioNegocio.BD::PER.Persona"						  AS PER ON (PER."IdPersona"=USU."IdPersona")
INNER JOIN   "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" AS CCDG ON (CCDG."IdEmpresa"=:IdEmpresa) 
WHERE "DiasTranscurridos" =:DiasTranscurridos
group BY  USU."IdPersona",PER."IdDireccionElectronicaPrincipal",USU."NombreCompleto",CCDG."RazonSocial",CCDG."Telefono";