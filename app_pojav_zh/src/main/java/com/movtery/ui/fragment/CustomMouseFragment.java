package com.movtery.ui.fragment;

import static com.movtery.utils.PojavZHTools.DIR_CUSTOM_MOUSE;
import static com.movtery.utils.PojavZHTools.copyFileInBackground;
import static com.movtery.utils.PojavZHTools.isImage;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.ui.subassembly.filelist.FileIcon;
import com.movtery.ui.subassembly.filelist.FileItemBean;
import com.movtery.ui.subassembly.filelist.FileRecyclerViewCreator;
import com.movtery.ui.subassembly.recyclerview.SpacesItemDecoration;

import net.kdt.pojavlaunch.PojavApplication;

import com.movtery.utils.PojavZHTools;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import com.movtery.ui.dialog.FilesDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CustomMouseFragment extends Fragment {
    public static final String TAG = "CustomMouseFragment";
    private final List<FileItemBean> mData = new ArrayList<>();
    private ActivityResultLauncher<String[]> openDocumentLauncher;
    private Button mReturnButton, mAddFileButton, mRefreshButton;
    private ImageButton mHelpButton;
    private ImageView mMouseView;
    private FileRecyclerViewCreator fileRecyclerViewCreator;

    public CustomMouseFragment() {
        super(R.layout.fragment_custom_mouse);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                result -> {
                    if (result != null) {
                        Toast.makeText(requireContext(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show();

                        PojavApplication.sExecutorService.execute(() -> {
                            copyFileInBackground(requireContext(), result, mousePath().getAbsolutePath());

                            runOnUiThread(() -> {
                                Toast.makeText(requireContext(), getString(R.string.zh_file_added), Toast.LENGTH_SHORT).show();
                                loadData();
                            });
                        });
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        bindViews(view);
        loadData();

        mReturnButton.setOnClickListener(v -> PojavZHTools.onBackPressed(requireActivity()));
        mAddFileButton.setOnClickListener(v -> openDocumentLauncher.launch(new String[]{"image/*"}));

        mRefreshButton.setOnClickListener(v -> loadData());
        mHelpButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

            builder.setTitle(getString(R.string.zh_help_mouse_title));
            builder.setMessage(getString(R.string.zh_help_mouse_message));
            builder.setPositiveButton(getString(R.string.zh_help_ok), null);

            builder.show();
        });
    }

    private void loadData() {
        runOnUiThread(() -> {
            fileRecyclerViewCreator.loadData(FileRecyclerViewCreator.loadItemBeansFromPath(requireContext(), mousePath(), FileIcon.IMAGE, true, false));
            //默认显示当前选中的鼠标
            refreshIcon();
        });
    }

    private File mousePath() {
        File path = new File(DIR_CUSTOM_MOUSE);
        if (!path.exists()) PojavZHTools.mkdirs(path);
        return path;
    }

    private void refreshIcon() {
        PojavApplication.sExecutorService.execute(() -> runOnUiThread(() -> mMouseView.setImageDrawable(PojavZHTools.customMouse(requireContext()))));
    }

    private void bindViews(@NonNull View view) {
        mReturnButton = view.findViewById(R.id.zh_custom_mouse_return_button);
        mAddFileButton = view.findViewById(R.id.zh_custom_mouse_add_button);
        mRefreshButton = view.findViewById(R.id.zh_custom_mouse_refresh_button);
        mHelpButton = view.findViewById(R.id.zh_custom_mouse_help_button);
        mMouseView = view.findViewById(R.id.zh_custom_mouse_icon);

        RecyclerView mMouseListView = view.findViewById(R.id.zh_custom_mouse);
        fileRecyclerViewCreator = new FileRecyclerViewCreator(requireContext(), new SpacesItemDecoration(0, 0, 0, (int) Tools.dpToPx(8)), mMouseListView, (position, file, name) -> {
            String fileName = file.getName();
            boolean isDefaultMouse = fileName.equals("default_mouse.png");

            FilesDialog.FilesButton filesButton = new FilesDialog.FilesButton();
            filesButton.setButtonVisibility(false, false, !isDefaultMouse, !isDefaultMouse, !isDefaultMouse, isImage(file)); //默认虚拟鼠标不支持分享、重命名、删除操作

            //如果选中的虚拟鼠标是默认的虚拟鼠标，那么将加上额外的提醒
            String message = getString(R.string.zh_file_message);
            if (isDefaultMouse)
                message += "\n" + getString(R.string.zh_custom_mouse_message_default);

            filesButton.messageText = message;
            filesButton.moreButtonText = getString(R.string.global_select);

            FilesDialog filesDialog = new FilesDialog(requireContext(), filesButton, this::loadData, file);
            filesDialog.setMoreButtonClick(v -> {
                DEFAULT_PREF.edit().putString("custom_mouse", fileName).apply();
                refreshIcon();
                Toast.makeText(requireContext(), getString(R.string.zh_custom_mouse_added) + fileName, Toast.LENGTH_SHORT).show();
                filesDialog.dismiss();
            });
            filesDialog.show();
        }, null, mData);
    }
}