SET SCHEMA "BSCL";

/*==============================================================*/
/* Drop Sequence:                                               */
/*==============================================================*/
DROP SEQUENCE "BSCL.SocioNegocio.BD::REL.Secuencias::NormativaRelacionClasificacion"     CASCADE;
DROP SEQUENCE "BSCL.SocioNegocio.BD::REL.Secuencias::NormativaRelacion"     CASCADE;


/*==============================================================*/
/* Create Sequence:                                             */
/*==============================================================*/

CREATE SEQUENCE "BSCL.SocioNegocio.BD::REL.Secuencias::NormativaRelacionClasificacion" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.SocioNegocio.BD::REL.Secuencias::NormativaRelacion" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;