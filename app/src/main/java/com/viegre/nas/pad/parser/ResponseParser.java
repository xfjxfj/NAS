package com.viegre.nas.pad.parser;

import com.viegre.nas.pad.entity.ResponseEntity;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.Response;
import rxhttp.wrapper.annotation.Parser;
import rxhttp.wrapper.entity.ParameterizedTypeImpl;
import rxhttp.wrapper.exception.ParseException;
import rxhttp.wrapper.parse.AbstractParser;
import rxhttp.wrapper.utils.Converter;

/**
 * Created by レインマン on 2021/04/28 16:32 with Android Studio.
 */
@Parser(name = "Response")
public class ResponseParser<T> extends AbstractParser<T> {

	/**
	 * 此构造方法适用于任意Class对象，但更多用于带泛型的Class对象，如：List<Student>
	 * <p>
	 * 用法:
	 * Java: .asParser(new ResponseParser<List<Student>>(){})
	 * Kotlin: .asParser(object : ResponseParser<List<Student>>() {})
	 * <p>
	 * 注：此构造方法一定要用protected关键字修饰，否则调用此构造方法将拿不到泛型类型
	 */
	protected ResponseParser() {}

	/**
	 * 此构造方法仅适用于不带泛型的Class对象，如: Student.class
	 * <p>
	 * 用法
	 * Java: .asParser(new ResponseParser<>(Student.class))   或者  .asResponse(Student.class)
	 * Kotlin: .asParser(ResponseParser(Student::class.java)) 或者  .asResponse(Student::class.java)
	 */
	public ResponseParser(@NotNull Type type) {
		super(type);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T onParse(@NotNull Response response) throws IOException {
		//处理泛型类型
		ResponseEntity<T> data = Converter.convert(response, ParameterizedTypeImpl.get(ResponseEntity.class, mType));
		//获取data字段
		T t = data.getData();
		/*
		 * 考虑到有些时候服务端会返回：{"errorCode":0,"errorMsg":"关注成功"}等类似没有data的数据，
		 * 此时code正确，但是data字段为空，直接返回data的话，会报空指针错误，
		 * 所以，判断泛型为String类型时，重新赋值，并确保赋值不为null
		 */
		if (null == t && String.class == mType) {
			t = (T) data.getMsg();
		}
		//code不等于0，说明数据不正确，抛出异常
		if (0 != data.getCode() || null == t) {
			throw new ParseException(String.valueOf(data.getCode()), data.getMsg(), response);
		}
		return t;
	}
}
