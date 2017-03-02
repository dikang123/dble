package io.mycat.plan.common.item.function;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mycat.config.ErrorCode;
import io.mycat.plan.common.CastTarget;
import io.mycat.plan.common.CastType;
import io.mycat.plan.common.MySQLcom;
import io.mycat.plan.common.exception.MySQLOutPutException;
import io.mycat.plan.common.item.Item;
import io.mycat.plan.common.item.function.bitfunc.ItemFuncBitCount;
import io.mycat.plan.common.item.function.castfunc.ItemDateTypecast;
import io.mycat.plan.common.item.function.castfunc.ItemDatetimeTypecast;
import io.mycat.plan.common.item.function.castfunc.ItemDecimalTypecast;
import io.mycat.plan.common.item.function.castfunc.ItemFuncBinary;
import io.mycat.plan.common.item.function.castfunc.ItemFuncSigned;
import io.mycat.plan.common.item.function.castfunc.ItemFuncUnsigned;
import io.mycat.plan.common.item.function.castfunc.ItemNCharTypecast;
import io.mycat.plan.common.item.function.castfunc.ItemTimeTypecast;
import io.mycat.plan.common.item.function.mathsfunc.ItemFuncAbs;
import io.mycat.plan.common.item.function.mathsfunc.ItemFuncAcos;
import io.mycat.plan.common.item.function.mathsfunc.ItemFuncAsin;
import io.mycat.plan.common.item.function.mathsfunc.ItemFuncAtan;
import io.mycat.plan.common.item.function.mathsfunc.ItemFuncCeiling;
import io.mycat.plan.common.item.function.mathsfunc.ItemFuncConv;
import io.mycat.plan.common.item.function.mathsfunc.ItemFuncCos;
import io.mycat.plan.common.item.function.mathsfunc.ItemFuncCot;
import io.mycat.plan.common.item.function.mathsfunc.ItemFuncCrc32;
import io.mycat.plan.common.item.function.mathsfunc.ItemFuncDegree;
import io.mycat.plan.common.item.function.mathsfunc.ItemFuncExp;
import io.mycat.plan.common.item.function.mathsfunc.ItemFuncFloor;
import io.mycat.plan.common.item.function.mathsfunc.ItemFuncLn;
import io.mycat.plan.common.item.function.mathsfunc.ItemFuncLog;
import io.mycat.plan.common.item.function.mathsfunc.ItemFuncLog10;
import io.mycat.plan.common.item.function.mathsfunc.ItemFuncLog2;
import io.mycat.plan.common.item.function.mathsfunc.ItemFuncMd5;
import io.mycat.plan.common.item.function.mathsfunc.ItemFuncPi;
import io.mycat.plan.common.item.function.mathsfunc.ItemFuncPow;
import io.mycat.plan.common.item.function.mathsfunc.ItemFuncRadians;
import io.mycat.plan.common.item.function.mathsfunc.ItemFuncRand;
import io.mycat.plan.common.item.function.mathsfunc.ItemFuncRound;
import io.mycat.plan.common.item.function.mathsfunc.ItemFuncSign;
import io.mycat.plan.common.item.function.mathsfunc.ItemFuncSin;
import io.mycat.plan.common.item.function.mathsfunc.ItemFuncSqrt;
import io.mycat.plan.common.item.function.mathsfunc.ItemFuncTan;
import io.mycat.plan.common.item.function.operator.cmpfunc.ItemFuncCoalesce;
import io.mycat.plan.common.item.function.operator.cmpfunc.ItemFuncGreatest;
import io.mycat.plan.common.item.function.operator.cmpfunc.ItemFuncIsnull;
import io.mycat.plan.common.item.function.operator.cmpfunc.ItemFuncLeast;
import io.mycat.plan.common.item.function.operator.cmpfunc.ItemFuncStrcmp;
import io.mycat.plan.common.item.function.operator.controlfunc.ItemFuncIfnull;
import io.mycat.plan.common.item.function.operator.controlfunc.ItemFuncNullif;
import io.mycat.plan.common.item.function.strfunc.ItemFuncBitLength;
import io.mycat.plan.common.item.function.strfunc.ItemFuncCharLength;
import io.mycat.plan.common.item.function.strfunc.ItemFuncConcat;
import io.mycat.plan.common.item.function.strfunc.ItemFuncConcatWs;
import io.mycat.plan.common.item.function.strfunc.ItemFuncElt;
import io.mycat.plan.common.item.function.strfunc.ItemFuncField;
import io.mycat.plan.common.item.function.strfunc.ItemFuncFindInSet;
import io.mycat.plan.common.item.function.strfunc.ItemFuncHex;
import io.mycat.plan.common.item.function.strfunc.ItemFuncInstr;
import io.mycat.plan.common.item.function.strfunc.ItemFuncLength;
import io.mycat.plan.common.item.function.strfunc.ItemFuncLocate;
import io.mycat.plan.common.item.function.strfunc.ItemFuncLower;
import io.mycat.plan.common.item.function.strfunc.ItemFuncLpad;
import io.mycat.plan.common.item.function.strfunc.ItemFuncReverse;
import io.mycat.plan.common.item.function.strfunc.ItemFuncRpad;
import io.mycat.plan.common.item.function.strfunc.ItemFuncSoundex;
import io.mycat.plan.common.item.function.strfunc.ItemFuncSpace;
import io.mycat.plan.common.item.function.strfunc.ItemFuncSubstrIndex;
import io.mycat.plan.common.item.function.strfunc.ItemFuncTrim;
import io.mycat.plan.common.item.function.strfunc.ItemFuncTrim.TRIM_TYPE_ENUM;
import io.mycat.plan.common.item.function.strfunc.ItemFuncUnhex;
import io.mycat.plan.common.item.function.strfunc.ItemFuncUpper;
import io.mycat.plan.common.item.function.timefunc.ItemFuncAddTime;
import io.mycat.plan.common.item.function.timefunc.ItemFuncConvTz;
import io.mycat.plan.common.item.function.timefunc.ItemFuncDateFormat;
import io.mycat.plan.common.item.function.timefunc.ItemFuncDatediff;
import io.mycat.plan.common.item.function.timefunc.ItemFuncDayname;
import io.mycat.plan.common.item.function.timefunc.ItemFuncDayofmonth;
import io.mycat.plan.common.item.function.timefunc.ItemFuncDayofweek;
import io.mycat.plan.common.item.function.timefunc.ItemFuncDayofyear;
import io.mycat.plan.common.item.function.timefunc.ItemFuncFromDays;
import io.mycat.plan.common.item.function.timefunc.ItemFuncFromUnixtime;
import io.mycat.plan.common.item.function.timefunc.ItemFuncLastDay;
import io.mycat.plan.common.item.function.timefunc.ItemFuncMakedate;
import io.mycat.plan.common.item.function.timefunc.ItemFuncMaketime;
import io.mycat.plan.common.item.function.timefunc.ItemFuncMonthname;
import io.mycat.plan.common.item.function.timefunc.ItemFuncPeriodAdd;
import io.mycat.plan.common.item.function.timefunc.ItemFuncPeriodDiff;
import io.mycat.plan.common.item.function.timefunc.ItemFuncSecToTime;
import io.mycat.plan.common.item.function.timefunc.ItemFuncStrToDate;
import io.mycat.plan.common.item.function.timefunc.ItemFuncTimeToSec;
import io.mycat.plan.common.item.function.timefunc.ItemFuncTimediff;
import io.mycat.plan.common.item.function.timefunc.ItemFuncToDays;
import io.mycat.plan.common.item.function.timefunc.ItemFuncToSeconds;
import io.mycat.plan.common.item.function.timefunc.ItemFuncUnixTimestamp;
import io.mycat.plan.common.item.function.timefunc.ItemFuncWeekday;
import io.mycat.plan.common.item.function.timefunc.ItemFuncWeekofyear;
import io.mycat.plan.common.item.function.timefunc.ItemFuncYearweek;
import io.mycat.plan.common.time.MyTime;

public class ItemCreate {
	private Map<String, ItemFunc> nativFuncs = new HashMap<String, ItemFunc>();
	private static ItemCreate _instance = null;

	protected ItemCreate() {
		nativFuncs.put("ABS", new ItemFuncAbs(null));
		nativFuncs.put("ACOS", new ItemFuncAcos(null));
		nativFuncs.put("ADDTIME", new ItemFuncAddTime(null, false, false));
		// proFuncs.put("AES_DECRYPT", new Item_func_abs(null));
		// proFuncs.put("AES_ENCRYPT", new Item_func_abs(null));
		// proFuncs.put("ANY_VALUE", new Item_func_abs(null));
		// proFuncs.put("AREA", new Item_func_abs(null));
		// proFuncs.put("ASBINARY", new Item_func_abs(null));
		nativFuncs.put("ASIN", new ItemFuncAsin(null));
		// proFuncs.put("ASTEXT", new Item_func_abs(null));
		// proFuncs.put("ASWKB", new Item_func_abs(null));
		// proFuncs.put("ASWKT", new Item_func_abs(null));
		nativFuncs.put("ATAN", new ItemFuncAtan(null));
		nativFuncs.put("ATAN2", new ItemFuncAtan(null));
		// proFuncs.put("BENCHMARK", new Item_func_abs(null));
		nativFuncs.put("BIN", new ItemFuncConv(null));
		nativFuncs.put("BIT_COUNT", new ItemFuncBitCount(null));
		// proFuncs.put("BUFFER", new Item_func_abs(null));
		nativFuncs.put("BIT_LENGTH", new ItemFuncBitLength(null));
		nativFuncs.put("CEIL", new ItemFuncCeiling(null));
		nativFuncs.put("CEILING", new ItemFuncCeiling(null));
		// proFuncs.put("CENTROID", new Item_func_abs(null));
		nativFuncs.put("CHARACTER_LENGTH", new ItemFuncCharLength(null));
		nativFuncs.put("CHAR_LENGTH", new ItemFuncCharLength(null));
		// proFuncs.put("COERCIBILITY", new Item_func_abs(null));
		// proFuncs.put("COMPRESS", new Item_func_abs(null));
		nativFuncs.put("COALESCE", new ItemFuncCoalesce(null));
		nativFuncs.put("CONCAT", new ItemFuncConcat(null));
		nativFuncs.put("CONCAT_WS", new ItemFuncConcatWs(null));
		// proFuncs.put("CONNECTION_ID", new Item_func_abs(null));
		nativFuncs.put("CONV", new ItemFuncConv(null));
		nativFuncs.put("CONVERT_TZ", new ItemFuncConvTz(null));
		// proFuncs.put("CONVEXHULL", new Item_func_abs(null));
		nativFuncs.put("COS", new ItemFuncCos(null));
		nativFuncs.put("COT", new ItemFuncCot(null));
		nativFuncs.put("CRC32", new ItemFuncCrc32(null));
		// proFuncs.put("CROSSES", new Item_func_abs(null));
		nativFuncs.put("DATEDIFF", new ItemFuncDatediff(null));
		nativFuncs.put("DATE_FORMAT", new ItemFuncDateFormat(null, false));
		nativFuncs.put("DAYNAME", new ItemFuncDayname(null));
		nativFuncs.put("DAYOFMONTH", new ItemFuncDayofmonth(null));
		nativFuncs.put("DAYOFWEEK", new ItemFuncDayofweek(null));
		nativFuncs.put("DAYOFYEAR", new ItemFuncDayofyear(null));
		// proFuncs.put("DECODE", new Item_func_decode(null));
		nativFuncs.put("DEGREES", new ItemFuncDegree(null));
		// proFuncs.put("DES_DECRYPT", new Item_func_abs(null));
		// proFuncs.put("DES_ENCRYPT", new Item_func_abs(null));
		// proFuncs.put("DIMENSION", new Item_func_abs(null));
		// proFuncs.put("DISJOINT", new Item_func_abs(null));
		// proFuncs.put("DISTANCE", new Item_func_abs(null));
		nativFuncs.put("ELT", new ItemFuncElt(null));
		// proFuncs.put("ENCODE", new Item_func_abs(null));
		// proFuncs.put("ENCRYPT", new Item_func_abs(null));
		// proFuncs.put("ENDPOINT", new Item_func_abs(null));
		// proFuncs.put("ENVELOPE", new Item_func_abs(null));
		// proFuncs.put("EQUALS", new Item_func_abs(null));
		nativFuncs.put("EXP", new ItemFuncExp(null));
		// proFuncs.put("EXPORT_SET", new Item_func_abs(null));
		// proFuncs.put("EXTERIORRING", new Item_func_abs(null));
		// proFuncs.put("EXTRACTVALUE", new Item_func_abs(null));
		nativFuncs.put("FIELD", new ItemFuncField(null));
		nativFuncs.put("FIND_IN_SET", new ItemFuncFindInSet(null));
		nativFuncs.put("FLOOR", new ItemFuncFloor(null));
		// proFuncs.put("FOUND_ROWS", new Item_func_(null));
		// proFuncs.put("FROM_BASE64", new Item_func_(null));
		nativFuncs.put("FROM_DAYS", new ItemFuncFromDays(null));
		nativFuncs.put("FROM_UNIXTIME", new ItemFuncFromUnixtime(null));
		// proFuncs.put("GEOMCOLLFROMTEXT", new Item_func_abs(null));
		// proFuncs.put("GEOMCOLLFROMWKB", new Item_func_abs(null));
		// proFuncs.put("GEOMETRYCOLLECTIONFROMTE
		// proFuncs.put("GEOMETRYCOLLECTIONFROMWK
		// proFuncs.put("GEOMETRYFROMTEXT", new Item_func_abs(null));
		// proFuncs.put("GEOMETRYFROMWKB", new Item_func_abs(null));
		// proFuncs.put("GEOMETRYN", new Item_func_abs(null));
		// proFuncs.put("GEOMETRYTYPE", new Item_func_abs(null));
		// proFuncs.put("GEOMFROMTEXT", new Item_func_abs(null));
		// proFuncs.put("GEOMFROMWKB", new Item_func_abs(null));
		// proFuncs.put("GET_LOCK", new Item_func_abs(null));
		// proFuncs.put("GLENGTH", new Item_func_abs(null));
		nativFuncs.put("GREATEST", new ItemFuncGreatest(null));
		// proFuncs.put("GTID_SUBTRACT", new Item_func_abs(null));
		// proFuncs.put("GTID_SUBSET", new Item_func_abs(null));
		nativFuncs.put("HEX", new ItemFuncHex(null));
		nativFuncs.put("IFNULL", new ItemFuncIfnull(null));
		// proFuncs.put("INET_ATON", new Item_func_abs(null));
		// proFuncs.put("INET_NTOA", new Item_func_abs(null));
		// proFuncs.put("INET6_ATON", new Item_func_abs(null));
		// proFuncs.put("INET6_NTOA", new Item_func_abs(null));
		// proFuncs.put("IS_IPV4", new Item_func_abs(null));
		// proFuncs.put("IS_IPV6", new Item_func_abs(null));
		// proFuncs.put("IS_IPV4_COMPAT", new Item_func_abs(null));
		// proFuncs.put("IS_IPV4_MAPPED", new Item_func_abs(null));
		nativFuncs.put("INSTR", new ItemFuncInstr(null));
		// proFuncs.put("INTERIORRINGN", new Item_func_abs(null));
		// proFuncs.put("INTERSECTS", new Item_func_abs(null));
		// proFuncs.put("ISCLOSED", new Item_func_abs(null));
		// proFuncs.put("ISEMPTY", new Item_func_abs(null));
		nativFuncs.put("ISNULL", new ItemFuncIsnull(null));
		// proFuncs.put("ISSIMPLE", new Item_func_abs(null));
		// proFuncs.put("JSON_VALID", new Item_func_abs(null));
		// proFuncs.put("JSON_CONTAINS", new Item_func_abs(null));
		// proFuncs.put("JSON_CONTAINS_PATH", new Item_func_abs(null));
		// proFuncs.put("JSON_LENGTH", new Item_func_abs(null));
		// proFuncs.put("JSON_DEPTH", new Item_func_abs(null));
		// proFuncs.put("JSON_TYPE", new Item_func_abs(null));
		// proFuncs.put("JSON_KEYS", new Item_func_abs(null));
		// proFuncs.put("JSON_EXTRACT", new Item_func_abs(null));
		// proFuncs.put("JSON_ARRAY_APPEND", new Item_func_abs(null));
		// proFuncs.put("JSON_INSERT", new Item_func_abs(null));
		// proFuncs.put("JSON_ARRAY_INSERT", new Item_func_abs(null));
		// proFuncs.put("JSON_OBJECT", new Item_func_abs(null));
		// proFuncs.put("JSON_SEARCH", new Item_func_abs(null));
		// proFuncs.put("JSON_SET", new Item_func_abs(null));
		// proFuncs.put("JSON_REPLACE", new Item_func_abs(null));
		// proFuncs.put("JSON_ARRAY", new Item_func_abs(null));
		// proFuncs.put("JSON_REMOVE", new Item_func_abs(null));
		// proFuncs.put("JSON_MERGE", new Item_func_abs(null));
		// proFuncs.put("JSON_QUOTE", new Item_func_abs(null));
		// proFuncs.put("JSON_UNQUOTE", new Item_func_abs(null));
		// proFuncs.put("IS_FREE_LOCK", new Item_func_abs(null));
		// proFuncs.put("IS_USED_LOCK", new Item_func_abs(null));
		nativFuncs.put("LAST_DAY", new ItemFuncLastDay(null));
		// proFuncs.put("LAST_INSERT_ID", new Item_func_abs(null));
		nativFuncs.put("LCASE", new ItemFuncLower(null));
		nativFuncs.put("LEAST", new ItemFuncLeast(null));
		nativFuncs.put("LENGTH", new ItemFuncLength(null));

		// proFuncs.put("LIKE_RANGE_MIN", new Item_func_(null));
		// proFuncs.put("LIKE_RANGE_MAX", new Item_func_abs(null));

		// proFuncs.put("LINEFROMTEXT", new Item_func_abs(null));
		// proFuncs.put("LINEFROMWKB", new Item_func_abs(null));
		// proFuncs.put("LINESTRINGFROMTEXT", new Item_func_abs(null));
		// proFuncs.put("LINESTRINGFROMWKB", new Item_func_abs(null));
		nativFuncs.put("LN", new ItemFuncLn(null));
		// proFuncs.put("LOAD_FILE", new Item_func_abs(null));
		nativFuncs.put("LOCATE", new ItemFuncLocate(null));
		nativFuncs.put("LOG", new ItemFuncLog(null));
		nativFuncs.put("LOG10", new ItemFuncLog10(null));
		nativFuncs.put("LOG2", new ItemFuncLog2(null));
		nativFuncs.put("LOWER", new ItemFuncLower(null));
		nativFuncs.put("LPAD", new ItemFuncLpad(null));
		nativFuncs.put("LTRIM", new ItemFuncTrim(null, TRIM_TYPE_ENUM.LTRIM));
		nativFuncs.put("MAKEDATE", new ItemFuncMakedate(null));
		nativFuncs.put("MAKETIME", new ItemFuncMaketime(null));

		nativFuncs.put("MD5", new ItemFuncMd5(null));
		nativFuncs.put("MONTHNAME", new ItemFuncMonthname(null));
		nativFuncs.put("NULLIF", new ItemFuncNullif(null, null));
		nativFuncs.put("OCT", new ItemFuncLog2(null));
		nativFuncs.put("PERIOD_ADD", new ItemFuncPeriodAdd(null));
		nativFuncs.put("PERIOD_DIFF", new ItemFuncPeriodDiff(null));
		nativFuncs.put("PI", new ItemFuncPi(null));
		nativFuncs.put("POW", new ItemFuncPow(null));
		nativFuncs.put("POWER", new ItemFuncPow(null));
		nativFuncs.put("RADIANS", new ItemFuncRadians(null));
		nativFuncs.put("RAND", new ItemFuncRand(null));
		nativFuncs.put("REVERSE", new ItemFuncReverse(null));
		nativFuncs.put("ROUND", new ItemFuncRound(null));
		nativFuncs.put("RPAD", new ItemFuncRpad(null));
		nativFuncs.put("RTRIM", new ItemFuncTrim(null, TRIM_TYPE_ENUM.RTRIM));
		nativFuncs.put("SEC_TO_TIME", new ItemFuncSecToTime(null));
		nativFuncs.put("SIGN", new ItemFuncSign(null));
		nativFuncs.put("SIN", new ItemFuncSin(null));

		nativFuncs.put("SOUNDEX", new ItemFuncSoundex(null));
		nativFuncs.put("SPACE", new ItemFuncSpace(null));
		nativFuncs.put("SQRT", new ItemFuncSqrt(null));
		nativFuncs.put("STRCMP", new ItemFuncStrcmp(null, null));
		nativFuncs.put("STR_TO_DATE", new ItemFuncStrToDate(null));
		nativFuncs.put("SUBSTRING_INDEX", new ItemFuncSubstrIndex(null));
		nativFuncs.put("SUBTIME", new ItemFuncAddTime(null, false, true));
		nativFuncs.put("TAN", new ItemFuncTan(null));
		nativFuncs.put("TIMEDIFF", new ItemFuncTimediff(null));
		nativFuncs.put("TIME_FORMAT", new ItemFuncDateFormat(null, true));
		nativFuncs.put("TIME_TO_SEC", new ItemFuncTimeToSec(null));
		nativFuncs.put("TO_DAYS", new ItemFuncToDays(null));
		nativFuncs.put("TO_SECONDS", new ItemFuncToSeconds(null));
		nativFuncs.put("UCASE", new ItemFuncUpper(null));
		nativFuncs.put("UNHEX", new ItemFuncUnhex(null));
		nativFuncs.put("UNIX_TIMESTAMP", new ItemFuncUnixTimestamp(null));
		nativFuncs.put("UPPER", new ItemFuncUpper(null));
//		nativFuncs.put("VERSION", new ItemFuncVersion());
		nativFuncs.put("WEEKDAY", new ItemFuncWeekday(null));
		nativFuncs.put("WEEKOFYEAR", new ItemFuncWeekofyear(null));
		nativFuncs.put("YEARWEEK", new ItemFuncYearweek(null));
	}

	public static synchronized ItemCreate getInstance() {
		if (_instance == null)
			_instance = new ItemCreate();
		return _instance;
	}

	public boolean isNativeFunc(String funcName) {
		return nativFuncs.containsKey(funcName.toUpperCase());
	}

	public ItemFunc createNativeFunc(String funcName, List<Item> args) {
		ItemFunc nf = nativFuncs.get(funcName.toUpperCase());
		return nf.nativeConstruct(args);
	}

	public ItemFunc create_func_cast(Item a, CastType type) {
		CastTarget cast_type = type.target;
		ItemFunc res = null;
		switch (cast_type) {
		case ITEM_CAST_BINARY:
			res = new ItemFuncBinary(a,type.length);
			break;
		case ITEM_CAST_SIGNED_INT:
			res = new ItemFuncSigned(a);
			break;
		case ITEM_CAST_UNSIGNED_INT:
			res = new ItemFuncUnsigned(a);
			break;
		case ITEM_CAST_DATE:
			res = new ItemDateTypecast(a);
			break;
		case ITEM_CAST_TIME:
		case ITEM_CAST_DATETIME: {
			if (type.length > MyTime.DATETIME_MAX_DECIMALS)
				throw new MySQLOutPutException(ErrorCode.ER_OPTIMIZER, "",
						"too big precision in cast time/datetime,max 6,current:" + type.length);
			res = (cast_type == CastTarget.ITEM_CAST_TIME) ? new ItemTimeTypecast(a, type.length)
					: new ItemDatetimeTypecast(a, type.length);
			break;
		}
		case ITEM_CAST_DECIMAL: {
			if (type.length < type.dec) {
				throw new MySQLOutPutException(ErrorCode.ER_OPTIMIZER, "",
						"For float(m,d), double(m,d) or decimal(m,d), M must be >= d");
			}
			if (type.length > MySQLcom.DECIMAL_MAX_PRECISION) {
				throw new MySQLOutPutException(ErrorCode.ER_OPTIMIZER, "",
						"Too big precision " + type.length + " max is " + MySQLcom.DECIMAL_MAX_PRECISION);
			}
			if (type.dec > MySQLcom.DECIMAL_MAX_SCALE) {
				throw new MySQLOutPutException(ErrorCode.ER_OPTIMIZER, "",
						"Too big scale " + type.dec + " max is " + MySQLcom.DECIMAL_MAX_SCALE);
			}
			res = new ItemDecimalTypecast(a, type.length, type.dec);
			break;
		}
		case ITEM_CAST_NCHAR: {
			int len = -1;
			if (type.length > 0)
				len = type.length;
			res = new ItemNCharTypecast(a, len);
			break;
		}
		default: {
			assert (false);
			break;
		}
		}
		return res;
	}

	
}