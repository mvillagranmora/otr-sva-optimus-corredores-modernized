set schema BSCL;
 
 select "DocumentoIdentidad",* from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"
 where "DocumentoIdentidad"  in ( 
 '96666140-2'
,'90249000-0'
,'96518240-3'
,'96551730-8'
,'96572920-8'
,'80993900-6')
  
select * 
from "BSCL.SocioNegocio.BD::PER.Persona" AS A1
,"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as A2
WHERE A1."IdDocumentoIdentidad" = A2."IdDocumentoIdentidad"
AND A2."DocumentoIdentidad" in ( 
 '96666140-2'
,'90249000-0'
,'96518240-3'
,'96551730-8'
,'96572920-8'
,'80993900-6')

UPDATE A1 SET A1."IdDireccionElectronicaPrincipal" = A2."IdDireccionElectronica"
FROM
"BSCL.SocioNegocio.BD::PER.Persona" AS A1
,(select A1."Direccion",A3."IdPersona","IdDireccionElectronica" 
from "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS A1
,"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as A2
,"BSCL.SocioNegocio.BD::PER.Persona" as A3
WHERE A2."IdDocumentoIdentidad" = A3."IdDocumentoIdentidad"
AND   A3."IdPersona" = A1."IdPersona"
AND A2."DocumentoIdentidad" in ( 
 '96666140-2'
,'90249000-0'
,'96518240-3'
,'96551730-8'
,'96572920-8'
,'80993900-6')) AS A2
WHERE A1."IdPersona" = A2."IdPersona"
 
UPDATE A1 set A1."RazonSocial" = 'Depósito Central de Valores S.A'
from "BSCL.SocioNegocio.BD::PER.Persona" AS A1
,"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as A2
WHERE A1."IdDocumentoIdentidad" = A2."IdDocumentoIdentidad"
AND A2."DocumentoIdentidad" = '96666140-2';

UPDATE A1 set A1."RazonSocial" = 'Bolsa Comercio Santiago'
from "BSCL.SocioNegocio.BD::PER.Persona" AS A1
,"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as A2
WHERE A1."IdDocumentoIdentidad" = A2."IdDocumentoIdentidad"
AND A2."DocumentoIdentidad" = '90249000-0';

UPDATE A1 set A1."RazonSocial" = 'Bolsa de Comercio de Valparaiso'
from "BSCL.SocioNegocio.BD::PER.Persona" AS A1
,"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as A2
WHERE A1."IdDocumentoIdentidad" = A2."IdDocumentoIdentidad"
AND A2."DocumentoIdentidad" = '96518240-3';

UPDATE A1  set A1."RazonSocial" = 'Bolsa Electronica de Comercio'
from "BSCL.SocioNegocio.BD::PER.Persona" AS A1
,"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as A2
WHERE A1."IdDocumentoIdentidad" = A2."IdDocumentoIdentidad"
AND A2."DocumentoIdentidad" = '96551730-8';

UPDATE A1 set A1."RazonSocial" = 'Contraparte Central S.A'
from "BSCL.SocioNegocio.BD::PER.Persona" AS A1
,"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as A2
WHERE A1."IdDocumentoIdentidad" = A2."IdDocumentoIdentidad"
AND A2."DocumentoIdentidad" = '96572920-8';

-- REVISAMOS LAS CONTRAPARTES
select "RazonSocial",* from "BSCL"."BSCL.SocioNegocio.BD::CNT.Contraparte" as A1
,"BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as A2
WHERE A2."IdPersona" = A1."IdPersona"

-- DEBEMOS ACTULIZAR LAS CONTRAPARTES EN LA TABLAS DE CONCEPTOS 
--DE CUSTODIA

SELECT * FROM "BSCL"."BSCL.Custodia.BD::CUS.ConceptoCustodia"

-- ACTUALIZAMOS LOS CONCEPTOS CUSTODIA
UPDATE A1 SET A1."IdContraparte" = (select A1."IdContraparte"
from "BSCL.SocioNegocio.BD::CNT.Contraparte" AS A1
,"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as A2
,"BSCL.SocioNegocio.BD::PER.Persona" as A3
WHERE A2."IdDocumentoIdentidad" = A3."IdDocumentoIdentidad"
AND   A3."IdPersona" = A1."IdPersona"
AND A2."DocumentoIdentidad" = '90249000-0')--bolsa de comercio de santiago
FROM "BSCL"."BSCL.Custodia.BD::CUS.ConceptoCustodia" AS A1
WHERE A1."NombreEntidad" ='CUS.GARANTIA'
AND A1."IdConcepto" = 'GAINSI';

UPDATE A1 SET A1."IdContraparte" = (select A1."IdContraparte"
from "BSCL.SocioNegocio.BD::CNT.Contraparte" AS A1
,"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as A2
,"BSCL.SocioNegocio.BD::PER.Persona" as A3
WHERE A2."IdDocumentoIdentidad" = A3."IdDocumentoIdentidad"
AND   A3."IdPersona" = A1."IdPersona"
AND A2."DocumentoIdentidad" = '90249000-0')--bolsa de comercio de santiago
FROM "BSCL"."BSCL.Custodia.BD::CUS.ConceptoCustodia" AS A1
WHERE A1."NombreEntidad" ='CUS.GARANTIA'
AND A1."IdConcepto" = 'GAINSP';

UPDATE A1 SET A1."IdContraparte" = (select A1."IdContraparte"
from "BSCL.SocioNegocio.BD::CNT.Contraparte" AS A1
,"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as A2
,"BSCL.SocioNegocio.BD::PER.Persona" as A3
WHERE A2."IdDocumentoIdentidad" = A3."IdDocumentoIdentidad"
AND   A3."IdPersona" = A1."IdPersona"
AND A2."DocumentoIdentidad" = '96572920-8')--CCLV
FROM "BSCL"."BSCL.Custodia.BD::CUS.ConceptoCustodia" AS A1
WHERE A1."NombreEntidad" ='CUS.GARANTIA'
AND A1."IdConcepto" = 'GAINFU';


UPDATE A1 SET A1."IdContraparte" = (select A1."IdContraparte"
from "BSCL.SocioNegocio.BD::CNT.Contraparte" AS A1
,"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as A2
,"BSCL.SocioNegocio.BD::PER.Persona" as A3
WHERE A2."IdDocumentoIdentidad" = A3."IdDocumentoIdentidad"
AND   A3."IdPersona" = A1."IdPersona"
AND A2."DocumentoIdentidad" = '90249000-0')--bolsa de comercio de santiago
FROM "BSCL"."BSCL.Custodia.BD::CUS.ConceptoCustodia" AS A1
WHERE A1."NombreEntidad" ='CUS.GARANTIA'
AND A1."IdConcepto" IN ('GAINFW','GAINVC','GAINRF');

-- ACTUALIZAMOS LA BOLSA TIPO INSTRUMENTO

SELECT * FROM "BSCL"."BSCL.Plataforma.BD::INS.BolsaTipoInstrumento"







