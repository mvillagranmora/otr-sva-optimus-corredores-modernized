SET SCHEMA "BSCL";

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4000, 'EJERCICIO', 'NUMBER', 0, 4, 'S', '^\d{4}$', 'Formato invalido', 0, 'N', 'N', '2024')
WHERE "IdFormato"=4000 AND "CodigoCampo"='EJERCICIO';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4001, 'MERCADO', 'STRING', 1, 2, 'S', '^.{0,3}$', 'Formato invalido', 0, 'N', 'N', 'AC')
WHERE "IdFormato"=4001 AND "CodigoCampo"='MERCADO';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4002, 'NEMO', 'STRING', 2, 40, 'S', '^.{0,40}$', 'Formato invalido', 0, 'N', 'N', 'COPEC')
WHERE "IdFormato"=4002 AND "CodigoCampo"='NEMO';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4003, 'FEC_PAGO', 'STRING', 3, 10, 'S', '^(?:\d{4}-\d{2}-\d{2})$', 'Formato invalido', 0, 'N', 'N', '2024-12-01')
WHERE "IdFormato"=4003 AND "CodigoCampo"='FEC_PAGO';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4004, 'SEC_EVE', 'NUMBER', 4, 10, 'S', '^[1-9]\d{0,9}$|^1$', 'Formato invalido', 0, 'N', 'N', '0123456789')
WHERE "IdFormato"=4004 AND "CodigoCampo"='SEC_EVE';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4005, 'DESCRIPCION', 'STRING', 5, 50, 'S', '^.{0,50}$', 'Formato invalido', 0, 'N', 'N', 'DIVIDENDO COPEC 123')
WHERE "IdFormato"=4005 AND "CodigoCampo"='DESCRIPCION';

/*
UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(3555, 'FACTOR_06', 'NUMBER', 6, 9, 'N', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato invalido', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=3555 AND "CodigoCampo"='FACTOR_06';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(3556, 'FACTOR_07', 'NUMBER', 7, 9, 'N', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato invalido', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=3556 AND "CodigoCampo"='FACTOR_07';
*/

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4006, 'FACTOR_08', 'NUMBER', 8, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato invalido', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4006 AND "CodigoCampo"='FACTOR_08';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4007, 'FACTOR_09', 'NUMBER', 9, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato invalido', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4007 AND "CodigoCampo"='FACTOR_09';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4008, 'FACTOR_10', 'NUMBER', 10, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato invalido', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4008 AND "CodigoCampo"='FACTOR_10';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4009, 'FACTOR_11', 'NUMBER', 11, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato invalido', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4009 AND "CodigoCampo"='FACTOR_11';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4010, 'FACTOR_12', 'NUMBER', 12, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato invalido', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4010 AND "CodigoCampo"='FACTOR_12';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4011, 'FACTOR_13', 'NUMBER', 13, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato invalido', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4011 AND "CodigoCampo"='FACTOR_13';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4012, 'FACTOR_14', 'NUMBER', 14, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato invalido', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4012 AND "CodigoCampo"='FACTOR_14';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4013, 'FACTOR_15', 'NUMBER', 15, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato invalido', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4013 AND "CodigoCampo"='FACTOR_15';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4014, 'FACTOR_16', 'NUMBER', 16, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato invalido', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4014 AND "CodigoCampo"='FACTOR_16';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4015, 'FACTOR_17', 'NUMBER', 17, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato invalido', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4015 AND "CodigoCampo"='FACTOR_17';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4016, 'FACTOR_18', 'NUMBER', 18, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato invalido', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4016 AND "CodigoCampo"='FACTOR_18';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4017, 'FACTOR_19', 'NUMBER', 19, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato invalido', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4017 AND "CodigoCampo"='FACTOR_19';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4018, 'FACTOR_20', 'NUMBER', 20, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato invalido', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4018 AND "CodigoCampo"='FACTOR_20';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4019, 'FACTOR_21', 'NUMBER', 21, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato invalido', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4019 AND "CodigoCampo"='FACTOR_21';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4020, 'FACTOR_22', 'NUMBER', 22, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato invalido', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4020 AND "CodigoCampo"='FACTOR_22';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4021, 'FACTOR_23', 'NUMBER', 23, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato invalido', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4021 AND "CodigoCampo"='FACTOR_23';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4022, 'FACTOR_24', 'NUMBER', 24, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato invalido', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4022 AND "CodigoCampo"='FACTOR_24';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4023, 'FACTOR_25', 'NUMBER', 25, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato invalido', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4023 AND "CodigoCampo"='FACTOR_25';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4024, 'FACTOR_26', 'NUMBER', 26, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato invalido', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4024 AND "CodigoCampo"='FACTOR_26';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4025, 'FACTOR_27', 'NUMBER', 27, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato invalido', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4025 AND "CodigoCampo"='FACTOR_27';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4026, 'FACTOR_28', 'NUMBER', 28, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato invalido', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4026 AND "CodigoCampo"='FACTOR_28';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4027, 'FACTOR_29', 'NUMBER', 29, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato incorrecto', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4027 AND "CodigoCampo"='FACTOR_29';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4028, 'FACTOR_30', 'NUMBER', 30, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato incorrecto', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4028 AND "CodigoCampo"='FACTOR_30';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4029, 'FACTOR_31', 'NUMBER', 31, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato incorrecto', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4029 AND "CodigoCampo"='FACTOR_31';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4030, 'FACTOR_32', 'NUMBER', 32, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato incorrecto', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4030 AND "CodigoCampo"='FACTOR_32';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4031, 'FACTOR_33', 'NUMBER', 33, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato incorrecto', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4031 AND "CodigoCampo"='FACTOR_33';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4032, 'FACTOR_34', 'NUMBER', 34, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato incorrecto', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4032 AND "CodigoCampo"='FACTOR_34';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4033, 'FACTOR_35', 'NUMBER', 35, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato incorrecto', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4033 AND "CodigoCampo"='FACTOR_35';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4034, 'FACTOR_36', 'NUMBER', 36, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato incorrecto', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4034 AND "CodigoCampo"='FACTOR_36';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4035, 'FACTOR_37', 'NUMBER', 37, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato incorrecto', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4035 AND "CodigoCampo"='FACTOR_37';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4037, 'FACTOR_38', 'NUMBER', 38, 9, 'S', '^(0(\.\d{1,8})?|1(\.0{1,8})?)$', 'Formato incorrecto', 8, 'S', 'N', '0.12345678')
WHERE "IdFormato"=4037 AND "CodigoCampo"='FACTOR_38';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Formato"
VALUES(4036, 'AcogidoISFUT', 'STRING', 39, 1, 'S', '^[SN]$', 'Formato incorrecto', 0, 'N', 'S', 'N')
WHERE "IdFormato"=4036 AND "CodigoCampo"='AcogidoISFUT';

COMMIT;