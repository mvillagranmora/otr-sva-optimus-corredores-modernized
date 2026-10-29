SET SCHEMA "BSCL"; 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca"; 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca"  ( "IdClasificacionEmpresaFatca","Nombre","PorDefecto","UsaNumeroGIIN") VALUES ( 'FFI','FFI','N','S'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca"  ( "IdClasificacionEmpresaFatca","Nombre","PorDefecto","UsaNumeroGIIN") VALUES ( 'FFIEXCEPT','FFI EXCEPTED','N','S'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca"  ( "IdClasificacionEmpresaFatca","Nombre","PorDefecto","UsaNumeroGIIN") VALUES ( 'NPFFI','NPFFI','N','S'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca"  ( "IdClasificacionEmpresaFatca","Nombre","PorDefecto","UsaNumeroGIIN") VALUES ( 'NFFEEXCEPT','NFFE EXCEPTED','N','S'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca"  ( "IdClasificacionEmpresaFatca","Nombre","PorDefecto","UsaNumeroGIIN") VALUES ( 'NFFEPASSIV','NFFE PASSIVE','N','S'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca"  ( "IdClasificacionEmpresaFatca","Nombre","PorDefecto","UsaNumeroGIIN") VALUES ( 'NFFERECALC','NFFE RECALCITRANTE','N','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca"  ( "IdClasificacionEmpresaFatca","Nombre","PorDefecto","UsaNumeroGIIN") VALUES ( 'PENDIENTE','PENDIENTE DE CLASIFICAR','S','S'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.SocioNegocio.BD::PER.ClasificacionFatca"; 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionFatca"  ( "IdClasificacionFatca","Nombre","PorDefecto") VALUES ( 'PENDIENTE','PENDIENTE DE CLASIFICAR','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionFatca"  ( "IdClasificacionFatca","Nombre","PorDefecto") VALUES ( 'USENTITY','US ENTITY','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionFatca"  ( "IdClasificacionFatca","Nombre","PorDefecto") VALUES ( 'NOUSENTITY','NON US ENTITY','S'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.ClasificacionFatca"  ( "IdClasificacionFatca","Nombre","PorDefecto") VALUES ( 'RECALCITR','RECALCITRANTE','N'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto"; 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto"  ( "IdPropositoMedioContacto","Descripcion") VALUES ( 'PART','PARTICULAR'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto"  ( "IdPropositoMedioContacto","Descripcion") VALUES ( 'COME','COMERCIAL'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto"  ( "IdPropositoMedioContacto","Descripcion") VALUES ( 'RECA','RECADOS'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto"  ( "IdPropositoMedioContacto","Descripcion") VALUES ( 'FACT','FACTURACION'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto"  ( "IdPropositoMedioContacto","Descripcion") VALUES ( 'CORR','CORRESPONDENCIA'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto"  ( "IdPropositoMedioContacto","Descripcion") VALUES ( 'CASI','Casilla Correos'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto"  ( "IdPropositoMedioContacto","Descripcion") VALUES ( 'LABO','Laboral'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.SocioNegocio.BD::PER.TipoGenero"; 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoGenero"  ( "IdTipoGenero","Descripcion") VALUES ( 'F','FEMENINO'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoGenero"  ( "IdTipoGenero","Descripcion") VALUES ( 'M','MASCULINO'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.SocioNegocio.BD::PER.TipoMedioContacto"; 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoMedioContacto"  ( "IdTipoMedioContacto","Descripcion","SubTipo") VALUES ( 'DFISICA','DIRECCION','DIRFIS'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoMedioContacto"  ( "IdTipoMedioContacto","Descripcion","SubTipo") VALUES ( 'DCASILLA','CASILLA','DIRFIS'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoMedioContacto"  ( "IdTipoMedioContacto","Descripcion","SubTipo") VALUES ( 'TFIJO','TELEFONO FIJO','TELE'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoMedioContacto"  ( "IdTipoMedioContacto","Descripcion","SubTipo") VALUES ( 'TMOVIL','TELEFONO CELULAR','TELE'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoMedioContacto"  ( "IdTipoMedioContacto","Descripcion","SubTipo") VALUES ( 'DEMAIL','EMAIL','DIRELEC'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoMedioContacto"  ( "IdTipoMedioContacto","Descripcion","SubTipo") VALUES ( 'DWEB','PAGINA WEB','DIRELEC'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoMedioContacto"  ( "IdTipoMedioContacto","Descripcion","SubTipo") VALUES ( 'DTWIT','TWITTER','DIRELEC'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoMedioContacto"  ( "IdTipoMedioContacto","Descripcion","SubTipo") VALUES ( 'DFACE','FACEBOOK','DIRELEC'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoMedioContacto"  ( "IdTipoMedioContacto","Descripcion","SubTipo") VALUES ( 'DGOOGLE','GOOGLE +','DIRELEC'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.SocioNegocio.BD::PER.TipoPersona"; 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoPersona"  ( "IdTipoPersona","Descripcion") VALUES ( 'N','PERSONA NATURAL'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoPersona"  ( "IdTipoPersona","Descripcion") VALUES ( 'J','PERSONA JURIDICA'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.SocioNegocio.BD::PER.TipoTelefono"; 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoTelefono"  ( "IdTipoTelefono","Nombre") VALUES ( 'FIJO','FIJO'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoTelefono"  ( "IdTipoTelefono","Nombre") VALUES ( 'MOVIL','MOVIL'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PER.TipoTelefono"  ( "IdTipoTelefono","Nombre") VALUES ( 'OTRO','OTRO'); 
 COMMIT; 
 END; 
 
 
