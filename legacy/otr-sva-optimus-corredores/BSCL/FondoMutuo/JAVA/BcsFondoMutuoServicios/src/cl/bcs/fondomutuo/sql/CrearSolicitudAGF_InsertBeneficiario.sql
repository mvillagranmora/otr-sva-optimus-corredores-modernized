INSERT INTO "BSCL.FondoMutuo.BD::FMM.InterfazAgf" (
"IdInterfazAgf"			 ,	"DocumentoIdentidad" ,	"CodigoPortafolio"	,	"NombreCompleto"			,
"IdEstado"				 ,  "TipoRegistro"		 , 	"FechaIngreso"		, 	"FechaIngresoRegistro"		,
"IdEmpresa"				 ,	"IdCliente"			 ,	"IdPortafolio"
)
VALUES
(
"BSCL.FondoMutuo.BD.Secuencias::InterfazAgf".NEXTVAL, :DocumentoIdentidad, :CodigoPortafolio, :NombreCompleto,
:IdEstado, :TipoRegistro, :FechaIngreso, CURRENT_UTCTIMESTAMP,
:IdEmpresa, :IdCliente, :IdPortafolio
)