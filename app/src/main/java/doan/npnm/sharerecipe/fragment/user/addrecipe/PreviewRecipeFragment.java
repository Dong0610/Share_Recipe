package doan.npnm.sharerecipe.fragment.user.addrecipe;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import doan.npnm.sharerecipe.adapter.DirectionsAdapter;
import doan.npnm.sharerecipe.adapter.ImageRecipeAdapter;
import doan.npnm.sharerecipe.adapter.IngridentsAdapter;
import doan.npnm.sharerecipe.app.AppViewModel;
import doan.npnm.sharerecipe.app.RecipeViewModel;
import doan.npnm.sharerecipe.base.BaseFragment;
import doan.npnm.sharerecipe.databinding.FragmentPreviewRecipeBinding;
import doan.npnm.sharerecipe.model.recipe.Recipe;
import doan.npnm.sharerecipe.utility.Constant;

public class PreviewRecipeFragment extends BaseFragment<FragmentPreviewRecipeBinding> {

    private AppViewModel viewModel;
    private RecipeViewModel recipeViewModel;

    public PreviewRecipeFragment(AppViewModel viewModel, RecipeViewModel recipeViewModel) {
        this.viewModel = viewModel;
        this.recipeViewModel = recipeViewModel;
    }

    private Recipe recipe;

    @Override
    protected FragmentPreviewRecipeBinding getBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentPreviewRecipeBinding.inflate(getLayoutInflater());
    }

    private DirectionsAdapter directionsAdapter;

    private IngridentsAdapter ingridentsAdapter;

    private ImageRecipeAdapter adapter;

    @Override
    protected void initView() {

        directionsAdapter = new DirectionsAdapter(DirectionsAdapter.DIR_TYPE.PREVIEW, null);
        binding.rcvDirection.setAdapter(directionsAdapter);
        adapter = new ImageRecipeAdapter(null);

        ingridentsAdapter = new IngridentsAdapter(IngridentsAdapter.IGR_TYPE.PREVIEW, null);
        binding.rcvIngrident.setAdapter(ingridentsAdapter);
        binding.listImg.setAdapter(adapter);

        recipeViewModel.recipeLiveData.observe(this, data -> {
            this.recipe = data;
            if (recipeViewModel.imgUri != null) {
                loadImage(recipeViewModel.imgUri, binding.imgProduct);
            } else {
                loadImage(data.ImgUrl, binding.imgProduct);
            }
            binding.nameOfRecipe.setText(data.Name == null ? "" : data.Name);
            binding.description.setText(data.Description == null ? "" : data.Description);
            binding.timePrepare.setText(data.PrepareTime.Time);
            binding.selectMinutePP.setText(data.PrepareTime.TimeType);
            binding.timeCook.setText(data.CookTime.Time);
            binding.selectMinutePP.setText(data.CookTime.TimeType);
            binding.txtLever.setText(data.Level);

            directionsAdapter.setItems(data.Directions);
            ingridentsAdapter.setItems(data.Ingredients);
        });
        recipeViewModel.listSelect.observe(this, data -> {
            int indexNull = data.indexOf(null);
            if (indexNull >= 0) {
                data.remove(indexNull);
            }
            adapter.setItems(data);
        });

    }


    @Override
    public void OnClick() {
        binding.backIcon.setOnClickListener(v -> closeFragment(PreviewRecipeFragment.this));
        binding.btnCancel.setOnClickListener(v -> closeFragment(PreviewRecipeFragment.this));
        binding.btnNext.setOnClickListener(v -> {
            loaddingDialog.show();
            startUploadData(new OnAddImageSuccess() {
                @Override
                public void onAddSuccess(String docID, String img, ArrayList<String> listUrl) {
                    recipe.ImagePreview = listUrl;
                    recipe.Id = docID;
                    recipeViewModel.recipeLiveData.postValue(recipe);
                    putDataRecipe(recipeViewModel.recipeLiveData.getValue());
                }
            });
        });
    }

    private interface OnAddImageSuccess {
        void onAddSuccess(String docID, String img, ArrayList<String> listUrl);
    }

    private void startUploadData(OnAddImageSuccess onAddImageSuccess) {
        String firestoreID = firestore.collection(Constant.RECIPE).document().getId();
        StorageReference reference = storage.getReference(Constant.RECIPE).child(firestoreID);
        ArrayList<String> listUrl = new ArrayList<>();

        viewModel.putImgToStorage(reference, recipeViewModel.imgUri, new AppViewModel.OnPutImageListener() {
            @Override
            public void onComplete(String urlApp) {
                recipe.ImgUrl = urlApp;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    recipeViewModel.listSelect.getValue().forEach(uri -> {
                        viewModel.putImgToStorage(reference, uri, new AppViewModel.OnPutImageListener() {
                            @Override
                            public void onComplete(String url) {
                                listUrl.add(url);
                                if (listUrl.size() == recipeViewModel.listSelect.getValue().size()) {
                                    onAddImageSuccess.onAddSuccess(firestoreID, urlApp, listUrl);
                                }
                            }

                            @Override
                            public void onFailure(String mess) {
                                showToast(mess);
                                loaddingDialog.dismiss();
                            }
                        });
                    });
                }
            }

            @Override
            public void onFailure(String mess) {
                showToast(mess);
                loaddingDialog.dismiss();
            }
        });

    }

    private void putDataRecipe(Recipe value) {
        firestore.collection(Constant.RECIPE)
                .document(value.Id)
                .set(recipe).addOnSuccessListener(task -> {
                    showToast("Add data success full");
                    loaddingDialog.dismiss();
                    viewModel.isAddRecipe.postValue(true);

                }).addOnFailureListener(e -> {
                    showToast(e.getMessage());
                    loaddingDialog.dismiss();
                });
    }
}



















