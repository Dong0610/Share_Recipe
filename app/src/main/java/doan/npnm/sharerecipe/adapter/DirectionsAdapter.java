package doan.npnm.sharerecipe.adapter;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import doan.npnm.sharerecipe.databinding.ItemDirectionViewBinding;
import doan.npnm.sharerecipe.databinding.ItemDirectionViewPreviewBinding;
import doan.npnm.sharerecipe.model.recipe.Directions;

public class DirectionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    final ArrayList<Directions> directions= new ArrayList<>();
    public OnDirectionEvent event;
    final DIR_TYPE dirType;

    public enum DIR_TYPE {
        EDIT,
        PREVIEW
    }

    public DirectionsAdapter( DIR_TYPE dirType,OnDirectionEvent event) {

        this.event = event;
        this.dirType = dirType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return dirType == DIR_TYPE.EDIT ? new DirectionViewholder(ItemDirectionViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false))
                : new PreviewDirectionViewholder(ItemDirectionViewPreviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (dirType == DIR_TYPE.EDIT) {
            ((DirectionViewholder) holder).onBind(directions.get(position), position);
        } else {
            ((PreviewDirectionViewholder) holder).onBind(directions.get(position));
        }
    }
    public void setItems(ArrayList<Directions> items) {
        directions.clear();
        directions.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return directions.size();
    }


    public class DirectionViewholder extends RecyclerView.ViewHolder {
        private final ItemDirectionViewBinding binding;

        DirectionViewholder(ItemDirectionViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void onBind(Directions item, int position) {
            binding.ingridenName.setText(item.Name);
            binding.ingridenName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    event.onNameChange(item, s.toString(), position);
                }
            });

            binding.icRemoveItem.setOnClickListener(v -> {
                event.onRemove(item, position);
            });
        }
    }


    public class PreviewDirectionViewholder extends RecyclerView.ViewHolder {
        private final ItemDirectionViewPreviewBinding binding;

        PreviewDirectionViewholder(ItemDirectionViewPreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @SuppressLint("SetTextI18n")
        void onBind(Directions item) {
            binding.ingridenName.setText(item.Name);
            binding.txtNumId.setText(""+item.Id);
        }
    }


    public interface OnDirectionEvent {
        void onNameChange(Directions directions, String value, int postion);

        void onRemove(Directions id, int pos);
    }
}
