INSERT INTO "BSCL.SocioNegocio.BD::CNT.Contraparte"  (
    "IdContraparte" ,
    "IdPersona" ,
    "IdEmpresa" ,
    "IdEstado" ,
    "CodigoInscripcion",
    "Emisor",
    "Depositos",
    "Bolsa",
    "Corredora",
    "CamaraDeCompensacion",
    "Banco", 
    "Boveda", 
    "FechaIngreso" ,
    "FechaCreacion" ,
    "FechaModificacion",
    "EsEnrolado",
    "IdUsuarioCreador",
    "Otros",
    "IdCarga"
 ) VALUES(
    :IdContraparte, --IdContraparte
    :IdPersona, --IdPersona
    :IdEmpresa, --IdEmpresa
    :IdEstado, --IdEstado
    :CNT_CodigoInscripcion,
    :CNT_Emisor,
    :CNT_Depositos,
    :CNT_Bolsa,
    :CNT_Corredora,
    :CNT_CamaraDeCompensacion,
    :CNT_Banco, 
    :CNT_Boveda, 
    CURRENT_UTCTIMESTAMP, --FechaIngreso
    CURRENT_UTCTIMESTAMP, --FechaCreacion
    CURRENT_UTCTIMESTAMP, --FechaModificacion
    :EsEnrolado, --EsEnrolado
    :IdPersonaUsuario, --IdUsuarioCreador
    :CNT_Otros,
    :IdCarga
 );