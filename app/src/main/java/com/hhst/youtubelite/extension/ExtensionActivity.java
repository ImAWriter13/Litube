package com.hhst.youtubelite.extension;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.hhst.youtubelite.R;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ExtensionActivity extends AppCompatActivity {
	private static final int TYPE_NAV = 0;
	private static final int TYPE_TOGGLE = 1;
	private static final int TYPE_COLOR_PICKER = 2;
	private static final int TYPE_TEXT_INPUT = 3;
	@Inject
	ExtensionManager manager;
	private final Deque<Extension> stack = new ArrayDeque<>();
	private final Adapter adapter = new Adapter();
	private Extension page;
	private MaterialToolbar toolbar;

	public static Intent intent(@NonNull android.content.Context context) {
		return new Intent(context, ExtensionActivity.class);
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_extension);

		View root = findViewById(R.id.root);
		ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
			var bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
			return insets;
		});

		toolbar = findViewById(R.id.toolbar);
		toolbar.setNavigationOnClickListener(v -> navigateBack());
		toolbar.inflateMenu(R.menu.extension_actions);
		toolbar.setOnMenuItemClickListener(this::onMenuItemClick);

		RecyclerView list = findViewById(R.id.recyclerView);
		list.setLayoutManager(new LinearLayoutManager(this));
		list.setAdapter(adapter);
		if (list.getItemAnimator() instanceof SimpleItemAnimator animator) {
			animator.setSupportsChangeAnimations(false);
		}

		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				navigateBack();
			}
		});

		showPage(Extension.root(), false);
	}

	private boolean onMenuItemClick(@NonNull MenuItem item) {
		if (item.getItemId() != R.id.action_reset) return false;
		new MaterialAlertDialogBuilder(this)
						.setTitle(R.string.reset_extension_title)
						.setMessage(R.string.reset_extension_message)
						.setPositiveButton(R.string.confirm, (d, w) -> {
							manager.resetToDefault();
							adapter.notifyDataSetChanged();
						})
						.setNegativeButton(R.string.cancel, null)
						.show();
		return true;
	}

	private void open(@NonNull Extension item) {
		if (!item.hasChildren()) return;
		showPage(item, true);
	}

	private void showPage(@NonNull Extension next, boolean push) {
		if (push && page != null) {
			stack.push(page);
		}
		page = next;
		toolbar.setTitle(next.title());
		adapter.submit(next.children());
	}

	private void navigateBack() {
		if (stack.isEmpty()) {
			finish();
			return;
		}
		page = stack.pop();
		toolbar.setTitle(page.title());
		adapter.submit(page.children());
	}

	private final class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
		@NonNull
		private List<Extension> items = List.of();

		void submit(@NonNull List<Extension> items) {
			this.items = items;
			notifyDataSetChanged();
		}

		@Override
		public int getItemViewType(int position) {
			Extension item = items.get(position);
			if (item.isColorPicker()) return TYPE_COLOR_PICKER;
			if (item.isTextInput()) return TYPE_TEXT_INPUT;
			return item.hasChildren() ? TYPE_NAV : TYPE_TOGGLE;
		}

		@Override
		public int getItemCount() {
			return items.size();
		}

		@NonNull
		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			if (viewType == TYPE_NAV) {
				return new NavHolder(inflater.inflate(R.layout.item_extension_nav, parent, false));
			} else if (viewType == TYPE_COLOR_PICKER) {
				return new ColorPickerHolder(inflater.inflate(R.layout.item_extension_color_picker, parent, false));
			} else if (viewType == TYPE_TEXT_INPUT) {
				return new TextInputHolder(inflater.inflate(R.layout.item_extension_text_input, parent, false));
			}
			return new ToggleHolder(inflater.inflate(R.layout.item_extension_toggle, parent, false));
		}

		@Override
		public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
			Extension item = items.get(position);
			if (holder instanceof NavHolder nav) {
				nav.bind(item);
			} else if (holder instanceof ColorPickerHolder cp) {
				cp.bind(item);
			} else if (holder instanceof TextInputHolder ti) {
				ti.bind(item);
			} else {
				((ToggleHolder) holder).bind(item);
			}
		}
	}

	private final class NavHolder extends RecyclerView.ViewHolder {
		private final TextView title;
		private final TextView summary;
		private final ImageView chevron;
		private final ImageView icon;

		private NavHolder(@NonNull View itemView) {
			super(itemView);
			title = itemView.findViewById(R.id.title);
			summary = itemView.findViewById(R.id.summary);
			chevron = itemView.findViewById(R.id.chevron);
			icon = itemView.findViewById(R.id.icon);
		}

		private void bind(@NonNull Extension item) {
			title.setText(item.title());
			if (item.summary() == 0) {
				summary.setVisibility(View.GONE);
			} else {
				summary.setVisibility(View.VISIBLE);
				summary.setText(item.summary());
			}
			if (item.icon() != 0) {
				icon.setVisibility(View.VISIBLE);
				icon.setImageResource(item.icon());
			} else {
				icon.setVisibility(View.GONE);
			}
			chevron.setVisibility(View.VISIBLE);
			itemView.setOnClickListener(v -> open(item));
		}
	}

	private final class ToggleHolder extends RecyclerView.ViewHolder {
		private final TextView title;
		private final TextView summary;
		private final SwitchMaterial toggle;

		private ToggleHolder(@NonNull View itemView) {
			super(itemView);
			title = itemView.findViewById(R.id.title);
			summary = itemView.findViewById(R.id.summary);
			toggle = itemView.findViewById(R.id.toggle);
		}

		private void bind(@NonNull Extension item) {
			title.setText(item.title());
			if (item.summary() == 0) {
				summary.setVisibility(View.GONE);
			} else {
				summary.setVisibility(View.VISIBLE);
				summary.setText(item.summary());
			}
			toggle.setOnCheckedChangeListener(null);
			toggle.setChecked(manager.isEnabled(item.key()));
			toggle.setOnCheckedChangeListener((buttonView, isChecked) -> manager.setEnabled(item.key(), isChecked));
			itemView.setOnClickListener(v -> toggle.toggle());
		}
	}

	private final class ColorPickerHolder extends RecyclerView.ViewHolder {
		private final TextView title;
		private final SwitchMaterial toggle;
		private final EditText colorInput;
		private final View colorPreview;
		private TextWatcher colorWatcher;

		private ColorPickerHolder(@NonNull View itemView) {
			super(itemView);
			title = itemView.findViewById(R.id.title);
			toggle = itemView.findViewById(R.id.toggle);
			colorInput = itemView.findViewById(R.id.colorInput);
			colorPreview = itemView.findViewById(R.id.colorPreview);
		}

		private void bind(@NonNull Extension item) {
			title.setText(item.title());
			String colorKey = item.children().get(0).key();

			toggle.setOnCheckedChangeListener(null);
			toggle.setChecked(manager.isEnabled(item.key()));
			toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
				manager.setEnabled(item.key(), isChecked);
				updateColorInputVisibility(isChecked);
			});

			if (colorWatcher != null) {
				colorInput.removeTextChangedListener(colorWatcher);
			}

			String currentColor = manager.getString(colorKey);
			colorInput.setText(currentColor);
			updateColorInputVisibility(manager.isEnabled(item.key()));
			updateColorPreview(currentColor);

			colorWatcher = new TextWatcher() {
				@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
				@Override public void afterTextChanged(Editable s) {}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					String hex = s.toString().trim();
					if (hex.matches("^#[0-9A-Fa-f]{6}$")) {
						manager.setString(colorKey, hex);
						updateColorPreview(hex);
					}
				}
			};
			colorInput.addTextChangedListener(colorWatcher);

			itemView.setOnClickListener(v -> toggle.toggle());
		}

		private void updateColorInputVisibility(boolean visible) {
			colorInput.setVisibility(visible ? View.VISIBLE : View.GONE);
			colorPreview.setVisibility(visible ? View.VISIBLE : View.GONE);
		}

		private void updateColorPreview(String hex) {
			try {
				colorPreview.setBackgroundColor(Color.parseColor(hex));
			} catch (Exception e) {
				colorPreview.setBackgroundColor(Color.GRAY);
			}
		}
	}

	private final class TextInputHolder extends RecyclerView.ViewHolder {
		private final TextView title;
		private final SwitchMaterial toggle;
		private final EditText textInput;
		private final View inputContainer;
		private TextWatcher watcher;

		private TextInputHolder(@NonNull View itemView) {
			super(itemView);
			title = itemView.findViewById(R.id.title);
			toggle = itemView.findViewById(R.id.toggle);
			textInput = itemView.findViewById(R.id.textInput);
			inputContainer = itemView.findViewById(R.id.inputContainer);
		}

		private void bind(@NonNull Extension item) {
			title.setText(item.title());
			String valueKey = item.children().get(0).key();

			toggle.setOnCheckedChangeListener(null);
			toggle.setChecked(manager.isEnabled(item.key()));
			toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
				manager.setEnabled(item.key(), isChecked);
				inputContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
			});

			if (watcher != null) {
				textInput.removeTextChangedListener(watcher);
			}

			textInput.setText(manager.getString(valueKey));
			inputContainer.setVisibility(manager.isEnabled(item.key()) ? View.VISIBLE : View.GONE);

			watcher = new TextWatcher() {
				@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
				@Override public void afterTextChanged(Editable s) {}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					manager.setString(valueKey, s.toString().trim());
				}
			};
			textInput.addTextChangedListener(watcher);

			itemView.setOnClickListener(v -> toggle.toggle());
		}
	}
}