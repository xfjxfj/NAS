package com.viegre.nas.pad.fragment.settings;

import com.blankj.utilcode.util.ColorUtils;
import com.djangoogle.framework.fragment.BaseFragment;
import com.viegre.nas.pad.R;
import com.viegre.nas.pad.adapter.ProtocolListAdapter;
import com.viegre.nas.pad.databinding.FragmentProtocolBinding;
import com.viegre.nas.pad.entity.ProtocolEntity;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * Created by レインマン on 2020/12/17 17:39 with Android Studio.
 */
public class ProtocolFragment extends BaseFragment<FragmentProtocolBinding> {

	@Override
	protected void initialize() {
		initList();
	}

	public static ProtocolFragment newInstance() {
		return new ProtocolFragment();
	}

	private void initList() {
		ProtocolListAdapter protocolListAdapter = new ProtocolListAdapter();
		mViewBinding.rvProtocol.setLayoutManager(new LinearLayoutManager(mActivity));
		mViewBinding.rvProtocol.addItemDecoration(new HorizontalDividerItemDecoration.Builder(mActivity).color(ColorUtils.getColor(R.color.protocol_divider))
		                                                                                                .size(1)
		                                                                                                .margin(30, 28)
		                                                                                                .build());
		mViewBinding.rvProtocol.setAdapter(protocolListAdapter);
		List<ProtocolEntity> list = new ArrayList<>();
		list.add(new ProtocolEntity("协议1", "https://www.baidu.com"));
		list.add(new ProtocolEntity("协议2", "https://www.baidu.com"));
		list.add(new ProtocolEntity("协议3", "https://www.baidu.com"));
		protocolListAdapter.setList(list);
	}
}
